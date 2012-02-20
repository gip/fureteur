
package fureteur

import fureteur.pipeline._

object Fureteur {

  def main(args:Array[String]) {
    println("Fureteur will now crawl URLs from '"+args(0)+"' and write the result to '"+args(1)+"'")
    val p= new Pipeline(2, args(0), args(1))
    p.start
  }

}