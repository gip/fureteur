// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

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

  def addn(kvs:List[(String, String)]) = {
    new Data( map ++ (kvs.map ( (kv) => (kv._1 -> JString(kv._2)) ) ) )
  }

  def get(s:String):String = {
    map(s) match {
      case JString(s) => s
      case _ => throw new WrongValueType
    }
  }

  def apply(s:String):String = {
	get(s)
  }

  def getOption(s:String): Option[String] = {
    try { Some(get(s)) }
    catch { case _:WrongValueType
	      |      _:NoSuchElementException => None }
  }

/*
  def getArrayLength(s:String) = {
    map(s) match {
      case JArray(l:List[JObject]) => l.length
      case _ => throw new WrongValueType
    }
  }

  def getArrayRow(s:String, n:Int) = {
    map(s) match {
      case JArray(l:List[JObject]) => Data.fromAST(l.toArray apply n)
      case _ => throw new WrongValueType
    }
  }
*/

  def unwrapArray(s:String) = {
    map(s) match {
      case JArray(l:List[JObject]) => l.map (Data.fromAST(_))
      case _ => throw new WrongValueType
    }
  }

  def exists(s:String) = {
    map contains s
  }

  def getObject(s:String) = {
    map(s) match {
      case o:JObject => Data.fromAST(o)
      case _ => throw new WrongValueType
    }
  } 

  def toJson():String = {
    val m= map.foldRight(List[JField]())( ( ( kv, acc ) => JField(kv._1,kv._2)::acc) )
    compact(render(JObject(m)))
  }

  def toBytes() = { toJson().getBytes }
}

object Data {
  def empty(): Data = { new Data(scala.collection.immutable.Map.empty) }

  def fromAST(ast:JValue) = {
    ast match {
      case JObject(l:List[JField]) =>
        new Data( scala.collection.immutable.Map.empty ++ l.map ( (f) => (f.name, f.value) ) )
      case _ => throw new WrongFormat
    }   
  }

  def fromJson(s:String): Data = {
    fromAST(parse(s))
  }

  def fromBytes(a:Array[Byte]) = {
    fromJson(new String(a))
  }
}
