// Build the pipeline
//

package fureteur.pipeline

import akka.actor._
import akka.actor.Actor._

import fureteur.data._
import fureteur.fileio._
import fureteur.amqpio._
import fureteur.config._
import fureteur.http._
import fureteur.control._

/*
class FilePipeline(nclients:Int, fin:String, fout:String) {

  val config= new Config(chan, 200)
  val control= new PipelineControl(config)

  val prefetch= actorOf( new fileBatchPrefetcher( fin, 50, 3, Some(1000L), control ) )
  val wb= actorOf( new fileBatchWriteback( fout, control ) )

  val mng= new HttpManager
  val clients= makeClients(nclients)
  
  def start() = {
    prefetch.start
    wb.start
    clients map (c => c.start)
  }
  
  def stop() = {
    prefetch.stop
    wb.stop
    clients map (c => c.stop)      
  }
  
  def status() = {}
  
  def makeClients(n:Int):List[ActorRef] = { if(n==0) { Nil } else { makeFetcher()::makeClients(n-1) } }
  
  def makeFetcher()= {
    actorOf( new HttpFetcher(10, 50, prefetch, wb, Some(1000L), mng) )
  }
}
*/

class AmqpPipeline(nclients:Int) {

  import com.rabbitmq.client._

  val factory= new ConnectionFactory() // This will come from the config 
  val conn= factory.newConnection()
  val chan= conn.createChannel()
  val config= new Config(chan, 200)
  val control= new PipelineControl(config)

  val prefetch= actorOf( new amqpBatchPrefetcher( 50, 3, Some(1000L), config, control ) )
  val wb= actorOf( new amqpBatchWriteback( config, control ) )

  val mng= new HttpManager
  val clients= makeClients(nclients)
  
  def start() = {
    prefetch.start
    wb.start
    clients map (c => c.start)
  }
  
  def stop() = {
    prefetch.stop
    wb.stop
    clients map (c => c.stop)      
  }
  
  def status() = {}
  
  def makeClients(n:Int):List[ActorRef] = { if(n==0) { Nil } else { makeFetcher()::makeClients(n-1) } }
  
  def makeFetcher()= {
    actorOf( new HttpFetcher(10, 50, prefetch, wb, Some(1000L), mng) )
  }
  
}
