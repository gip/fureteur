
package fureteur

import fureteur.pipeline._
import fureteur.config._


object Fureteur {

  def main(args:Array[String]) {
	LocalConf.registerLocalConfigs()
	
    val system= new System(Config.getConfig(args(0)))

    system.start
  }

}


object LocalConf {

  def registerLocalConfigs() = {
    List[String](c0, c1) map ( c => Config.registerConfig( Config.fromJson( c ) ) )
  }

  val c0= 
"""
{
  "conf" : "f2f",
  "instance" : "fureteur#{HOST}",
   
  "pipelines" : [
    {
	  "httpManager" : {
	      "max_connection" : "2",
	      "max_connection_per_route" : "2",
	      "min_interval_ms" : "1000"
	    },	
	
	  "prefetcher" : { "class" : "fileBatchPrefetcher",
		               "file_name" : "meetup",
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
	                 "file_name" : "meetup_res" 
	               }
	}
  ]
}
"""

  val c1= 
"""
{
  "conf" : "r2r",
  "instance" : "fureteur#{HOST}",
   
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
}