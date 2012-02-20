
package fureteur.prefetch

import fureteur.data._
import fureteur.sync._

// Taking URLs in batches from a file
//
class fileBatchPrefetcher(file: String, size:Int, thres:Int, timeout: Option[Long]) 
      extends genericBatchProducer[Data](size, thres, timeout) {

  val data= scala.io.Source.fromFile(file).getLines.toArray
  var index= 0
  
  override def getBatch(sz:Int):Option[List[Data]] = {
    if(index>data.size) { return None }
    index+= sz
    val l= data.slice(index-sz, index).toList
    val d= Data.empty
    Some( l map (e => d add ("URL", e) )  )
  }
}