

package fureteur.fileio

import akka.event.EventHandler

import fureteur.sync._
import fureteur.data._
import fureteur.control._

// Taking URLs in batches from a file
//
class fileBatchPrefetcher(file: String, size:Int, thres:Int, timeout: Option[Long], control: Control) 
      extends genericBatchProducer[Data](size, thres, timeout, control) {

  EventHandler.info(this, "Opening "+file)
  val data= scala.io.Source.fromFile(file).getLines.toArray
  var index= 0
  var batch= 0
  
  override def getBatch(sz:Int):Option[List[Data]] = {
    if(index>data.size) { return None }
    index+= sz
    batch+= 1
    val l= data.slice(index-sz, index).toList
    EventHandler.info(this, "Fetched "+l.length.toString+" message(s) from "+file)
    val d= Data.empty
    Some( l map (e => d addn List(("fetch_url", e),("batch", batch.toString)) )  )
  }
}


// 
class fileBatchWriteback(fname: String, control: Control) extends genericBatchReseller[Data](control) {

  val file = new java.io.FileWriter(fname)

  def resell(batch: List[Data]) = {
    batch match {
      case x::xs => { val s= x.toJson+"\n"; file.write(s); resell(xs) }
      case Nil => 
    }
  }

}