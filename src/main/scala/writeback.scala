
package fureteur.writeback

import java.io._

import fureteur.data._
import fureteur.sync._

// 
class fileBatchWriteback(fname: String) extends genericBatchReseller[Data] {

  val file = new java.io.FileWriter(fname)

  def resell(batch: List[Data]) = {
    batch match {
      case x::xs => { val s= x.toJSON+"\n"; file.write(s); println(s); resell(xs) }
      case Nil => 
    }
  }

}
