

package fureteur.config

import scala.collection.mutable._

import com.rabbitmq.client._
import fureteur.data._

class Config(d:Data) {
  val data= d

  def apply(s:String):String = {
    data get s
  }

  def getInt(s:String) = {
    (data get s).toInt
  }

  def getOption(s:String) = {
	data getOption s
  }

  def getLongOption(s:String) = {
	(data getOption s) match { 
      case Some(s) => Some(s.toLong) 
      case _ => None
	}
  }

  def getObject(s:String):Config = { new Config(data.getObject(s)) }

  def unwrapArray(s:String):List[Config] = {
	(data unwrapArray s) map (new Config(_))
  }

}

object Config {

  val configs = new HashMap[String, Config]()

  def registerConfig(c:Config):Unit = {
	println("Registering "+c("conf"))
    configs+= (c("conf") -> c)
  }

  def getConfig(s:String) = {
    configs(s)
  }

  def fromJson(s:String) = {
	new Config( Data.fromJson(s) )
  }

}
