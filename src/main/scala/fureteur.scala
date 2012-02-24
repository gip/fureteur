
package fureteur

import fureteur.pipeline._
import fureteur.config._
import fureteur.version._

object Fureteur {

  def main(args:Array[String]) {
	
	// Register local configs
	LocalConfig.registerLocalConfigs()
	
    var config= (None:Option[Config])

    try {
	  args(0) match {
		case "use" =>
		  config= Some( Config.getConfig(args(1)) )
		case "load" =>
		  config= Some( Config.fromJson(scala.io.Source.fromFile(args(1)).mkString) )
		case "show" =>
		  println(Config.dumpConfig(args(1)))
		  return
		case "list" =>
		  println("Available configs:")
          Config.showConfigs().map ( kv => println(" "+kv._1+"   # "+kv._2))
          return
		case "version" =>
		  println(Version.versionString)
		  return
	  }

    } catch {
	  case e:java.lang.ArrayIndexOutOfBoundsException =>
	    println("usage: fureteur use <config name>    # Start execution using a local config")
	    println("       fureteur load <config path>   # Start execution using the provided config file")
	    println("       fureteur show <config name>   # Dump a local config to STDOUT")
	    println("       fureteur list                 # Show available local config")
	    println("       fureteur version              # Show version")
	    return
	}

    val system= new System( config get ) 
    system.start
  }

}


object LocalConfig {

  def registerLocalConfigs() = {
    localConfigs map ( c => Config.registerConfig( c ) )
  }

  val c0= 
"""
{
  "conf" : "f2f",
  "description" : "File to file operation",
  "usage" : "f2f <input file> <output file>",
  "instance" : "fureteu",
   
  "pipelines" : [
    {
	  "httpManager" : {
	      "max_connection" : "2",
	      "max_connection_per_route" : "2",
	      "min_interval_ms" : "1000"
	    },	
	
	  "prefetcher" : { "class" : "fileBatchPrefetcher",
		               "file_name" : "fureteur_in",
		               "batch_size" : "50",
		               "threshold_in_batches" : "3",
		               "timeout_ms" : "1000"
	                },
	  "httpFetchers": [ { "httpmanager" : "httpmanager0",
		                 "threshold_in" : "10",
	                     "threshold_out" : "50",
	                     "timeout_ms" : "1000"
 	                 } ],
	  "writeback" :{ "class" : "fileBatchWriteback",
	                 "file_name" : "fureteur_out" 
	               }
	}
  ]
}
"""

  val c1= 
"""
{
  "conf" : "r2r",
  "description" : "Input/output from RabbitMQ",
  "usage" : "r2r",
  "instance" : "fureteur",
   
  "pipelines" : [
    {
	  "amqp" : { },
	  "httpManager" : {
	      "max_connection" : "2",
	      "max_connection_per_route" : "2",
	      "min_interval_ms" : "1000"
	    },	
	
	  "prefetcher" : { "class" : "amqpBatchPrefetcher",
		               "queue" : "rgFetchIn",
		               "batch_size" : "50",
		               "threshold_in_batches" : "3",
		               "timeout_ms" : "1000"
	                },
	  "httpFetchers": [ { "httpmanager" : "httpmanager0",
		                 "threshold_in" : "10",
	                     "threshold_out" : "50",
	                     "timeout_ms" : "1000"
 	                 } ],
	  "writeback" :{ "class" : "amqpBatchWriteback",
	                 "exchange" : "rgFetchOut"
	               }
	}
  ]
}
"""

  val localConfigs= List[String](c0, c1)
}