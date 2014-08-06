// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.sync

// We are using Akka actors
import akka.actor._
import akka.event.Logging
//import akka.util.duration._
import scala.concurrent.duration._

import fureteur.collection.FIFO
import fureteur.control.Control

// Control messages
abstract class Ctrl
case class StatsReq(handler:List[(String,String)]=>Unit) extends Ctrl // Get stats out of the actor model
case class Stats(l:List[(String,String)]) extends Ctrl                // Get stats as a message
case class NoData() extends Ctrl
case class PseudoTimeout() extends Ctrl
case class DataReq(req:ActorRef, n:Int) extends Ctrl
case class DataIn[T](req:ActorRef, e:List[T]) extends Ctrl
case class DataOut[T](req:ActorRef, e:List[T]) extends Ctrl


// Generic processor base class
// The processor gets data of type T and process them into data of type U
abstract class genericProcessor[T,U] (thres_in: Int,             // Input threshold
                                      thres_out: Int,            // Output threshold
                                      producer: ActorRef, 
                                      reseller: ActorRef,
                                      timeout: Option[Long]
                                      ) extends Actor {

  val log = Logging(context.system, this)
  timeout match { case Some(ms) => context.setReceiveTimeout(ms milliseconds) }
  val fifo= new FIFO[T](Some( (thres_in, request) ))
  var fifo_max= 0
  var processed= List[U]()
  var processed_count= 0
  var total_count= 0
  var partial_send_count= 0
  var send_count= 0
    
  def request(n:Int): Unit = {
    producer ! DataReq(self, n)
  }
  
  override def preStart() = {
    fifo init
  }
    
  def receive = {
    case DataIn(_, elems: List[T]) => { fifo pushn elems; processShift() }
    case StatsReq(handler) => handler(stats())
    case ReceiveTimeout => { processShift() }
    case _ => log.info("received unknown message")
  }
  
  def processShift(): Unit ={
    if(fifo.isEmpty) {
      if(processed_count>0) { send() }
      return
    }
    if(fifo.length>fifo_max) { fifo_max= fifo.length }
    try {
      while(true) {
        val e= fifo pop;
        processed= process(e)::processed; // Queueing up the answers
        processed_count+= 1;
        if (processed_count>=thres_out) { send() }
        total_count+= 1;
      }
    } catch {
      case e:NoSuchElementException => // The queue is empty, we need more stuff
    }    
  }
  
  def send() = {
    reseller ! DataOut(self, processed)
    processed= List[U]()
    send_count+= 1
    if(processed_count<thres_out) { partial_send_count+= 1 }
    processed_count= 0
  }

  def stats(): List[(String,String)] = {
    var l= getStats()
    l= ("total_processed", total_count.toString)::l
    l= ("fifo_max_length", fifo_max.toString)::l
    l= ("partial_send_count", partial_send_count.toString)::l
    l= ("send_count", send_count.toString)::l
    l
  }
  
  def process(in: T): U;
  def getStats(): List[(String,String)] = { List() }
}


// Generic producer base class (working in batches)
//
abstract class genericBatchProducer[T] (size:Int,              // Size of a batch
                                        thres: Int,            // Treshold (expressed in number of batches)
                                        timeout: Option[Long], // Timeout in ms
                                        control: Control       // Control class
                                       ) extends Actor {

  val log = Logging(context.system, this)
  timeout match { case Some(ms) => context.setReceiveTimeout(ms milliseconds) }
  var timeouts= 0
  var batches_sent= 0                             
                                           
  val fifo= new FIFO[List[T]](Some(thres, singleRequest))
  val reqfifo= new FIFO[ActorRef](None)
  
  def singleRequest(n:Int): Unit = {
    if(fifo.length<=thres) { requestBatch() }
  }
  
  def multiRequest(): Unit = {
    if(fifo.length<=thres && requestBatch()) { multiRequest() }
  }
    
  def receive = {
    case DataReq(req, _) => { reqfifo push req; handleRequests(); }
    case ReceiveTimeout => { timeouts+=1; handleRequests() }
    case StatsReq(handler) => handler(stats())
    case _ => log.info("received unknown message")
  }
  
  def handleRequests(): Unit = {
    // if(!control.acceptInput()) { return }
    (fifo.isEmpty, reqfifo.isEmpty) match {
      case (false, false) => { batches_sent+= 1; reqfifo.pop ! DataIn(self, fifo.pop); handleRequests() }
      case (true,  false) => { singleRequest(0); if(!fifo.isEmpty) { handleRequests() } }
      case (_,     true) => { multiRequest(); }
    }
  }

  override def preStart() = {
    init()
    multiRequest()
  }
  
  def requestBatch(): Boolean = {
    getBatch(size) match {
      case Some(l:List[T]) => { fifo push l; control.addCounter(l.length); return true }
      case None => { return false; } 
    } 
  }
  
  def stats(): List[(String,String)] = {
    ("timeouts",timeouts.toString)::("batches_sent",batches_sent.toString)::getStats()
  }
  
  def getBatch(n:Int): Option[List[T]] // This function MUST not block
  def getStats(): List[(String,String)] = { List() }
  def init() = {}
}

// Generic batch reseller
//
abstract class genericBatchReseller[T](control: Control) extends Actor {

  val log = Logging(context.system, this)
  var c=0
  
  def receive = {
    case DataOut(req, out:List[T]) => { val sz= out.length; c+= sz; resell(out); control.subCounter(sz) }
    case StatsReq(handler) => handler(stats())
    case _ => log.info("received unknown message")
  }
  
  def stats(): List[(String,String)] = {
    getStats()
  }
  
  def resell(d:List[T])
  def getStats(): List[(String,String)] = { List() }
}

