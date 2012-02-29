// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.http

import java.io._
import java.util.concurrent.TimeUnit
// Akka
import akka.actor._
import akka.actor.Actor._
import akka.event.EventHandler
// We're using Apache http 4.x
import org.apache.http._
import org.apache.http.conn.scheme._
import org.apache.http.impl.conn.tsccm._
import org.apache.http.impl.client._
import org.apache.http.impl.conn._
import org.apache.http.conn._
import org.apache.http.conn.params._
import org.apache.http.client.methods._
import org.apache.http.client.params._
import org.apache.http.util._
import org.apache.http.conn.routing._
import org.apache.http.protocol._
import org.apache.http.client._

import fureteur.sync._
import fureteur.time._
import fureteur.encoding._
import fureteur.data._
import fureteur.dummyssl._
import fureteur.config._
import fureteur.version._

class HttpManager(config:Config) {
    val http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
    val ssf = DummySSLScheme.getDummySSLScheme();
    val https = new Scheme("https", 443, ssf);
    val sr = new SchemeRegistry();
    sr.register(http);
    sr.register(https);
    val cm= new ThreadSafeClientConnManager(sr)
    cm.setMaxTotal( config.getInt("max_connection") );
    cm.setDefaultMaxPerRoute( config.getInt("max_connection_per_route")  );
    val client = new DefaultHttpClient(cm);
    client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
    val min_interval_ms= config.getInt("min_interval_ms")

  def getClient() = {
    client
  }

  def getMinInterval() = {
    min_interval_ms
  }
}

class HttpFetcher(config: Config,
                  producer: ActorRef,
                  reseller: ActorRef,
                  manager: HttpManager
                  )  extends genericProcessor[Data,Data](config.getInt("threshold_in"), config.getInt("threshold_out"), producer, reseller, config.getLongOption("timeout_ms")) {

  self.id= "processor-"+HttpFetcher.iid
  HttpFetcher.iid+= 1
  val client= manager.getClient
  val interval= manager.getMinInterval
  var last_fetch_ms= 0L
  

  def process(d:Data):Data = {
    var out= List[(String, String)]( ("fetch_version", Version.versionString), ("fetch_format_version", Version.formatVersionString))
    var error= "false"
    var retcode= ""
    val url= d get "fetch_url"
	try {
      Thread.sleep(interval)
      val t0= Time.msNow
      val get= new HttpGet(url)
      val ctx= new BasicHttpContext()  // Can this be re-used?
      val res= client.execute(get, ctx)
      val entity= res.getEntity()
      val page= EntityUtils.toByteArray(entity)
      val zpage= Encoding.byteToZippedString64(page)
      val status= res.getStatusLine()
      val code= status.getStatusCode()
    
      EntityUtils.consume(entity)
      val t1= Time.msNow
      last_fetch_ms= t1
      retcode= code.toString
      out= ("fetch_time", Time.sNow.toString)::
                 ("fetch_latency", (t1-t0).toString)::
                 ("fetch_size", page.length.toString)::
                 ("fetch_data", zpage)::
                 ("fetch_status_code", code.toString)::
                 ("fetch_status_line", status.toString)::
                 ("fetch_error", "false")::out
    } catch {
	  case e:Exception => { error="true"; out= ("fetch_error", "true")::("fetch_error_reason", "exception")::out }
	}
    EventHandler.info(this, "Fetching "+url+", fetch_error "+error+", status code "+retcode)
	d addn out
  }                                                                   
}

object HttpFetcher {
  var iid= 0;
}
