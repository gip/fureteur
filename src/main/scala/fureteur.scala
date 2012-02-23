
package fureteur

import fureteur.pipeline._
import fureteur.config._

object Fureteur {

  def main(args:Array[String]) {

    println(args(0))
    val mode = try { args(0) } catch { case _ => "amqp" }
    println("Fureteur starting with mode "+mode)
    mode match {
	  case "amqp" => amqp()
	  case "file" => file(args(1), args(2))
	  case  _ => println("unknown mode '"+mode+"'")
    }
    //println("Fureteur will now crawl URLs from '"+args(0)+"' and write the result to '"+args(1)+"'")
  } 

  def file(ff:String, ft:String) = {
    val cfg= new Config(2, 100)
    val p= new FilePipeline(ff, ft, cfg)
    p.start    
  }

  def amqp() = {
    val cfg= new Config(2, 100)
    val p= new AmqpPipeline(cfg)
    p.start
  }

}