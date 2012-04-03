// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.fileio

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import fureteur.sync._
import fureteur.data._
import fureteur.control._
import fureteur.config._

// Taking URLs in batches from a file
//
class fileBatchPrefetcher(config: Config, control: Control) 
      extends genericBatchProducer[Data](config.getInt("batch_size"), config.getInt("threshold_in_batches"), config.getLongOption("timeout_ms"), control) {

  //val log = Logging(context.system, this)
  val file= config("file_name")

  log.info("Opening "+file)
  val data= scala.io.Source.fromFile(file).getLines.toArray
  var index= 0
  var batch= 0
  
  override def getBatch(sz:Int):Option[List[Data]] = {
    if(index>data.size) { return None }
    index+= sz
    batch+= 1
    val l= data.slice(index-sz, index).toList
    log.info("Fetched "+l.length.toString+" entrie(s) from "+file)
    val d= Data.empty
    Some( l map (e => d ++ List(("fetch_url", e),("batch", batch.toString)) )  )
  }
}


// 
class fileBatchWriteback(config: Config, control: Control) extends genericBatchReseller[Data](control) {

  //val log = Logging(context.system, this)
  val fname= config("file_name")
  val file = new java.io.FileWriter(fname)

  def resell(batch: List[Data]) = {
    log.info("Writing "+batch.length.toString+" entrie(s) to "+file)
    def doit(b:List[Data]):Unit = {
      b match {
        case x::xs => { val s= x.toJson+"\n"; file.write(s); doit(xs) }
        case Nil => 
      }
    }
    doit(batch)
  }

}