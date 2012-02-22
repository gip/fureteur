

package fureteur.fileio

import fureteur.sync._
import fureteur.data._
import fureteur.control._

// Taking URLs in batches from a file
//
class fileBatchPrefetcher(file: String, size:Int, thres:Int, timeout: Option[Long], control: Control) 
      extends genericBatchProducer[Data](size, thres, timeout, control) {

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



// 
class fileBatchWriteback(fname: String, control: Control) extends genericBatchReseller[Data](control) {

  val file = new java.io.FileWriter(fname)

  def resell(batch: List[Data]) = {
    batch match {
      case x::xs => { val s= x.toJson+"\n"; file.write(s); println(s); resell(xs) }
      case Nil => 
    }
  }

}