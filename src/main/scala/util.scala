// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

import fureteur.data._
import com.rabbitmq.client._

object AmpqUtil {

  def pushURLList(a:List[String], exch: String) = {
    val conn= new ConnectionFactory().newConnection()
    val chan= conn.createChannel()
    
    a.foldLeft(1)( (n,e) => { chan.basicPublish(exch, "FetchIn", null, ( Data.empty addn List(("fetch_url",e),("meta", n.toString)) ).toBytes ); n+1 } )
    
    chan.close()
    conn.close()
  }

  def pushURLFile(f:String, exch:String) = {
    pushURLList( scala.io.Source.fromFile(f).getLines.toList, exch )
  }

}


