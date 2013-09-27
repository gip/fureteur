// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.pipeline

import akka.actor._
import akka.actor.Props
import akka.event.Logging

import fureteur.data._
import fureteur.fileio._
import fureteur.amqpio._
import fureteur.config._
import fureteur.http._
import fureteur.control._


case class ClassNotFound(klass:String) extends Exception

class System(config:Config) {

  // Build the system
  val asys= System.asys
  val name= config("instance")
  val control= new PipelineControl(config)

  val pipelines= 
    if(config exists "proxies") {
      val proxies= (config unwrapArray "proxies") map( p => (p("host"), p("port")) )
      proxies map( pkv =>
        (config unwrapArray "pipelines") map ( c => new Pipeline(c ++ (("proxy_host", pkv._1) :: ("proxy_port", pkv._2) :: Nil) , control, asys)) ) flatten
    }
    else (config unwrapArray "pipelines") map (new Pipeline(_, control, asys))

  def start():Unit = {
    pipelines map ( (p) => p.start )
  } 

}

object System {

  val asys= ActorSystem("Fureteur")
  var i= 0
  def unique(s:String) = {
    i+= 1
    s+i.toString
  }
}

class Pipeline(config:Config, control:Control, asys:ActorSystem) {


  val amqpConnection= { 
    (config exists "amqp") match {
      case true => newAmqpConnection(config.getObject("amqp"))
      case _ => null
    }
  }

  val prefetch= newPrefetcher(config.getObject("prefetcher"))

  val writeback= newWriteback(config.getObject("writeback"))

  val httpManager= new HttpManager(config.getObject("httpManager"), config)

  val fetchers= (config unwrapArray "httpFetchers") map ( f => asys.actorOf(Props(new HttpFetcher(f, prefetch, writeback, httpManager )) /*, name= "fetcher"*/ ) )

  def start():Unit = {
    //prefetch.start
    //writeback.start
    //fetchers map ( f => f.start )
  }

  def stop():Unit = {
    //prefetch.stop
    //writeback.stop
    //fetchers map ( f => f.stop )
  }

  def newPrefetcher(config:Config):ActorRef = {
	 val klass= config("class")
     klass match {
        case "fileBatchPrefetcher" => asys.actorOf( Props(new fileBatchPrefetcher( config, control ) ) )
        case "amqpBatchPrefetcher" => asys.actorOf( Props(new amqpBatchPrefetcher( config, control, amqpConnection._2 ) ) )
        case (e:String) => throw (new ClassNotFound(e))
      }
  }

  def  newWriteback(config:Config):ActorRef = {
     val klass= config("class")
     klass match {
        case "fileBatchWriteback" => asys.actorOf( Props(new fileBatchWriteback( config, control ) ) )
        case "amqpBatchWriteback" => asys.actorOf( Props(new amqpBatchWriteback( config, control, amqpConnection._2 ) ) )
        case (e:String) => throw (new ClassNotFound(e))
      }
  }

  def newAmqpConnection(config:Config) = {
    import com.rabbitmq.client._
    val factory= new ConnectionFactory() // This will come from the config
    /* factory.setUri() doesn't work, not sure why
     (config getOption "uri") match {
      case Some(uri:String) => //factory.setUri(uri)
      case _ => ()
    } */
    val l= List( ("user", factory.setUsername(_)),
                 ("password", factory.setPassword(_)),
                 ("host", factory.setHost(_)),
                 ("port", ( (s:String) => factory.setPort(s.toInt))),
                 ("virtualHost", factory.setVirtualHost(_))
               )
    l foreach ( x => if(config exists x._1) { x._2(config(x._1)) } )

    val conn= factory.newConnection()
    val chan= conn.createChannel()
    println(conn)
    println(chan)
    (conn, chan)
  }

}