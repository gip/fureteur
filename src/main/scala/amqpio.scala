// IO to AMQP (e.g. RabbitMQ)
//

package fureteur.amqpio

import com.rabbitmq.client._

import fureteur.config._
import fureteur.data._
import fureteur.sync._


// Prefetching an AMQP queue
//
class amqpBatchPrefetcher(size:Int, 
                          thres:Int, 
                          timeout: Option[Long],
                          cfg: Config) 
      extends genericBatchProducer[Data](size, thres, timeout) {
  
  val config= cfg
  val factory= new ConnectionFactory() // This will come from the config 
  val conn= factory.newConnection()
  val chan= conn.createChannel()
  val queue= "rgFetchInLinkedin"
  
  override def init() = {
    // InitAMPQ
  }
  
  class EmptyQueue extends Exception
  
  def getMessage():Data = {
    val autoAck= false
    val response= chan.basicGet(queue, autoAck)
    if (null==response) { throw new EmptyQueue }
    val ra= response.getBody()
    val deliveryTag= response.getEnvelope().getDeliveryTag()
    val d= Data.fromBytes(ra)
    d add ("fetch_in_delivery_tag", deliveryTag.toString)
  }
  
  override def getBatch(sz:Int):Option[List[Data]] = {
    
    def rec(c:Int, l:List[Data]):List[Data] = { try { if(0==c) { l } else { rec(c-1, getMessage()::l ) } } catch { case e:EmptyQueue => l} }
    
    val l= rec(sz, List[Data]())
    if(l isEmpty) { None } else { return Some(l) }
  }
  
}


// Writing back to an AMQP exchange
//
class amqpBatchWriteback(cfg: Config) extends genericBatchReseller[Data] {

  val config= cfg
  val factory= new ConnectionFactory() // This will come from the config 
  val conn= factory.newConnection()
  val chan= conn.createChannel()
  val exch= "rgFetchOut"

  def resell(batch: List[Data]) = {  
    batch match {
      case x::xs => {
        chan.basicPublish(exch, "", null, x.toBytes)
        chan.basicAck((x get "fetch_in_delivery_tag") toLong, false)
        resell(xs) 
      }
      case Nil => ()
    }
  }

}

