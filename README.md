Fureteur
========

[Fureteur](https://github.com/gip/fureteur) is a simple, configurable, fault-tolerant http crawler written is scala. The main features are:

* Configurable number of concurrent pipelines; each pipeline may include a configurable number of individual fetchers
* Reuse of http connection
* Modular implementation using [akka actors](http://akka.io/)
* URLs prefetching and data writeback are made in batch (batch size is configurable)
* Build on the robust [apache http client library](http://hc.apache.org/)
* Prefetching of URLs from file and/or AMQP queues as [RabbitMQ](http://www.rabbitmq.com/)
* Easy build with [sbt](https://github.com/harrah/xsbt/wiki)
* Configurable using a simple JSON file

Getting Started
---------------

First, let's checkout the project and show the usage:

```
bash-3.2$ git clone git://github.com/gip/fureteur.git
bash-3.2$ sbt "run"
[info] Loading project definition from /Users/gilles/gip/test/fureteur/project
[info] Set current project to fureteur (in build file:/Users/gilles/gip/test/fureteur/)
[info] Running fureteur.Fureteur 
usage: fureteur run <config name>    # Start execution using a local config
       fureteur load <config path>   # Start execution using the provided config file
       fureteur show <config name>   # Dump a local config to STDOUT
       fureteur list                 # Show available local config
       fureteur version              # Show version
[success] Total time: 0 s, completed Mar 29, 2012 4:34:25 PM
```

Now, let's show the list of available configuration file and display the first one:

```
bash-3.2$ sbt "run list"
[info] Loading project definition from /Users/gilles/gip/test/fureteur/project
[info] Set current project to fureteur (in build file:/Users/gilles/gip/test/fureteur/)
[info] Running fureteur.Fureteur list
Available configs:
 f2f   # File to file operation
 r2r   # Input/output from RabbitMQ
[success] Total time: 0 s, completed Mar 29, 2012 4:37:21 PM
bash-3.2$ sbt "run show f2f"
[info] Loading project definition from /Users/gilles/gip/test/fureteur/project
[info] Set current project to fureteur (in build file:/Users/gilles/gip/test/fureteur/)
[info] Running fureteur.Fureteur show f2f
-- This is an example configuration file for fureteur
{
  "conf" : "f2f",                                -- Configuration name
  "description" : "File to file operation",      -- Description
  "usage" : "f2f <input file> <output file>",    -- Usage
  "instance" : "fureteur",                       -- Instance name 
   
  "pipelines" : [                                -- Pipelines
    {
      "httpManager" : {                          -- The http connection manager
          "max_connection" : "2",
          "max_connection_per_route" : "2",
          "min_interval_ms" : "1000"
        },  
    
      "prefetcher" : { "class" : "fileBatchPrefetcher",     -- Prefetching from files
                       "file_name" : "fureteur_in",         -- Input file name
                       "batch_size" : "50",                 -- Batch size when retrieving items from files
                       "threshold_in_batches" : "3",        -- Threshold (expressed in number of bacthes)
                       "timeout_ms" : "1000"                -- Timeout in ms 
                    },
      "httpFetchers": [ { "threshold_in" : "10",            -- Input threshold
                          "threshold_out" : "50",           -- Output threshold
                          "timeout_ms" : "1000"             -- Output timeout
                        },
                        { "threshold_in" : "10",            -- Input threshold (for second fetcher)
                          "threshold_out" : "50",           -- Output threshold (for second fetcher)
                          "timeout_ms" : "1000"             -- Output timeout (for second fetcher)
                        }
                      ],
      "writeback" :{ "class" : "fileBatchWriteback",        -- Writing back to file
                     "file_name" : "fureteur_out"           -- File name
                   }
    }
  ]
}

[success] Total time: 0 s, completed Mar 29, 2012 4:38:30 PM
```

The configuration above basically prefetch URLs from the fureteur_in file, fetch them and write them back to the fetch_out file. All the different options should be self-explanatory. Don't hesitate to contact me for further details.

To run the configuration, just do:

```
bash-3.2$ sbt "run run f2f"
```

It is also possible to copy/paste the configuration into a local file (for instance ex.conf), modify it and start fureteur using:

```
bash-3.2$ sbt "run load f2f"
```

It is also possible to create a separate jar file and start fureteur without sbt - please refer to sbt documentation. The r2r configuration reads and writes from an AMQP-compliant queue.

Contact
-------

Created by [Gilles Pirio](https://github.com/gip). Feel free to contact me at [gip.github@gmail.com](mailto:gip.github@gmail.com) .Thanks to [@Entelo](https://twitter.com/Entelo) for supporting open-source development!
