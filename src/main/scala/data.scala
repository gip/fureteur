
package fureteur.data

import scala.collection.immutable._

class Data(m:HashMap[String,String]) {
  val map= m
    
  def add(k:String, v:String) = {
    new Data( map + (k->v) )
  }
  
  def get(s:String):String = {
    map(s)
  }
  
  def getOption(s:String): Option[String] = {
    map.get(s)
  }
  
  def getAsMap() = {
    map
  }
  
  def toJSON():String = {
    val s= ""
    val q= "\""
    map.foldLeft(s)( ((ss,kv)=> (if(ss=="") { "{" } else { "," })+q+kv._1+q+":"+q+kv._2+q+"," ) )+"}"
  }
}

object Data {
  def empty(): Data = { new Data(scala.collection.immutable.HashMap.empty) }
}