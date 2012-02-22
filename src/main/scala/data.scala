
package fureteur.data

import scala.collection.immutable._
import net.liftweb.json._

class WrongValueType extends Exception
class WrongFormat extends Exception

class Data(m:Map[String, JValue]) {

  class WrongValueType extends Exception

  val map= m

  def add(k:String, v:String) = {
    new Data( map + (k->JString(v)) )
  }

  def get(s:String):String = {
    map(s) match {
	  case JString(s) => s
	  case _ => throw new WrongValueType
	}
  }

  def getOption(s:String): Option[String] = {
    try { Some(get(s)) }
    catch { case e:WrongValueType => None }
  }

  def toJson():String = {
	val m= map.foldRight(List[JField]())( ( ( kv, acc ) => JField(kv._1,kv._2)::acc) )
    compact(render(JObject(m)))
  }

  def toBytes() = { toJson().getBytes }
}

object Data {
  def empty(): Data = { new Data(scala.collection.immutable.Map.empty) }

  def fromJson(s:String): Data = {
	parse(s) match {
      case JObject(l:List[JField]) =>
        new Data( scala.collection.immutable.Map.empty ++ l.map ( (f) => (f.name, f.value) ) )
      case _ => throw new WrongFormat
	}
  }

  def fromBytes(a:Array[Byte]) = {
    fromJson(new String(a))
  }
}
