
package fureteur.control

import fureteur.config._

trait Control {
  def addCounter(s:Int): Int
  def subCounter(s:Int): Int
  def getCounter():Int
  def acceptInput():Boolean
}

class PipelineControl(config:Config) extends Control {
  val counter= new java.util.concurrent.atomic.AtomicInteger(0)
  val limit= config.limit

  def addCounter(n:Int):Int = { counter.getAndAdd(n) }
  def subCounter(n:Int):Int = { counter.getAndAdd(-n) }
  def getCounter():Int = { counter.get() }
  def acceptInput():Boolean = { counter.get()<limit }
}