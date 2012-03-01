// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.amqpio

import akka.event.EventHandler
import com.rabbitmq.client._

import fureteur.config._
import fureteur.data._
import fureteur.sync._
import fureteur.control._
import fureteur.config._

// Prefetching an AMQP queue
//
class amqpBatchPrefetcher(config: Config,
                          control: Control,
                          chan: Channel) 
      extends genericBatchProducer[Data](config.getInt("batch_size"), config.getInt("threshold_in_batches"), config.getLongOption("timeout_ms"), control) {
  
  val queue= config("queue")
  
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
    EventHandler.info(this, "Fetched message from "+queue+" with delivery tag "+deliveryTag)
    val d= Data.fromBytes(ra)
    d add ("fetch_queue_delivery_tag", deliveryTag.toString)
  }
  
  override def getBatch(sz:Int):Option[List[Data]] = {
    def rec(c:Int, l:List[Data]):List[Data] = { try { if(0==c) { l } else { rec(c-1, getMessage()::l ) } } catch { case e:EmptyQueue => l } }

    val l= rec(sz, List[Data]())
    val ls= l.length
    if(ls>0) { EventHandler.info(this, "Fetched "+ls.toString+" message(s) from "+queue) }
    if(l isEmpty) { None } else { return Some(l) }
  }
  
}


// Writing back to an AMQP exchange
//
class amqpBatchWriteback(config: Config, control: Control, chan: Channel) extends genericBatchReseller[Data](control) {

  val exch= config("exchange")

  def resell(batch: List[Data]) = {  
    batch match {
      case x::xs => {
        val deliveryTag= (x get "fetch_queue_delivery_tag").toLong
        val key= try {
            (x get "fetch_status_code") match {
              case "200" => "200"
              case s if s.matches("^4\\d\\d") => "4xx"
              case _ => "Error" 
            }
        } catch { case _ => "Error" }
        chan.basicPublish(exch, key, MessageProperties.PERSISTENT_TEXT_PLAIN, x.toBytes)
        chan.basicAck(deliveryTag, false)
        EventHandler.info(this, "Publishing message to "+exch+" and acking delivery tag "+deliveryTag)
        resell(xs) 
      }
      case Nil => ()
    }
  }

}

