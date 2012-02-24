// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.collection

import scala.collection.mutable.Queue


// A mutable FIFO with optional alert callback
//
class FIFO[T](x:Option[(Int, Int => Unit)]) {

  val queue= new Queue[T]
  var requested= false
  var enabled= true
  val (th,f) = x match { case Some((i,f)) => (i,f)
                         case None => (-1,((x:Int) => Unit)) }

  def length() = { queue.size }

  def push(e:T) = { queue += e; requested= false; check() }

  def pushn(l:List[T]) = { queue ++= l;  requested= false; check() }

  def pop() : T = { check(); val v= queue.dequeue; check(); v }  

  def check() : Unit = {  
    val l= queue.size
    if (enabled && !requested && l<=th) { f(l); requested= true }
  }
  
  def setEnabled(e:Boolean) : Unit = {
    enabled= e; 
  }
  
  def isEmpty() : Boolean = {
    queue.isEmpty
  }

  def init() = { check() }
}