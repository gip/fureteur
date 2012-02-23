// IO to AMQP (e.g. RabbitMQ)
//

package fureteur.amqpio

import akka.event.EventHandler
import com.rabbitmq.client._

import fureteur.config._
import fureteur.data._
import fureteur.sync._
import fureteur.control._

// Prefetching an AMQP queue
//
class amqpBatchPrefetcher(size:Int, 
                          thres:Int, 
                          timeout: Option[Long],
                          chan: Channel,
                          control: Control) 
      extends genericBatchProducer[Data](size, thres, timeout, control) {
  
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
    EventHandler.info(this, "Fetched message from '"+queue+"' with delivery tag "+deliveryTag)
    val d= Data.fromBytes(ra)
    d add ("fetch_in_delivery_tag", deliveryTag.toString)
  }
  
  override def getBatch(sz:Int):Option[List[Data]] = {
    
    def rec(c:Int, l:List[Data]):List[Data] = { try { if(0==c) { l } else { rec(c-1, getMessage()::l ) } } catch { case e:EmptyQueue => l} }
    
    val l= rec(sz, List[Data]())
    val ls= l.length
    if(ls>0) { EventHandler.info(this, "Fetched "+ls.toString+" message(s) from "+queue) }
    if(l isEmpty) { None } else { return Some(l) }
  }
  
}


// Writing back to an AMQP exchange
//
class amqpBatchWriteback(chan: Channel, control: Control) extends genericBatchReseller[Data](control) {

  val exch= "rgFetchOut"

  def resell(batch: List[Data]) = {  
    batch match {
      case x::xs => {
	    val deliveryTag= (x get "fetch_in_delivery_tag").toLong
        chan.basicPublish(exch, "", null, x.toBytes)
        chan.basicAck(deliveryTag, false)
        EventHandler.info(this, "Publishing message to '"+exch+"' with delivery tag "+deliveryTag)
        resell(xs) 
      }
      case Nil => ()
    }
  }

}

