

package fureteur.pipeline

import akka.actor._
import akka.actor.Actor._

import fureteur.data._
import fureteur.writeback._
import fureteur.prefetch._
import fureteur.http._


class Pipeline(nclients:Int, fin:String, fout:String) {
  val prefetch= actorOf( new fileBatchPrefetcher( fin, 50, 3, Some(1000L) ) )
  val wb= actorOf( new fileBatchWriteback( fout ) )
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


