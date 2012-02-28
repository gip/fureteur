// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.pipeline

import akka.actor._
import akka.actor.Actor._

import fureteur.data._
import fureteur.fileio._
import fureteur.amqpio._
import fureteur.config._
import fureteur.http._
import fureteur.control._


case class ClassNotFound(klass:String) extends Exception

class System(config:Config) {

  // Build the system
  var name = config("instance")
  val control= new PipelineControl(config)

  val pipelines= (config unwrapArray "pipelines") map (new Pipeline(_, control))

  def start():Unit = {
    pipelines map ( (p) => p.start )
  } 

}

class Pipeline(config:Config, control:Control) {


  val amqpConnection= { 
    (config exists "amqp") match {
      case true => newAmqpConnection(config.getObject("amqp"))
      case _ => null
    }
  }

  val prefetch= newPrefetcher(config.getObject("prefetcher"))

  val writeback= newWriteback(config.getObject("writeback"))

  val httpManager= new HttpManager(config.getObject("httpManager"))

  val fetchers= (config unwrapArray "httpFetchers") map ( f => actorOf( new HttpFetcher(f, prefetch, writeback, httpManager ) ) )

  def start():Unit = {
    prefetch.start
    writeback.start
    fetchers map ( f => f.start )
  }

  def stop():Unit = {
    prefetch.stop
    writeback.stop
    fetchers map ( f => f.stop )
  }

  def newPrefetcher(config:Config):ActorRef = {
     config("class") match {
        case "fileBatchPrefetcher" => actorOf( new fileBatchPrefetcher( config, control ) )
        case "amqpBatchPrefetcher" => actorOf( new amqpBatchPrefetcher( config, control, amqpConnection._2 ) )
        case (e:String) => throw (new ClassNotFound(e))
      }
  }

  def  newWriteback(config:Config):ActorRef = {
     config("class") match {
        case "fileBatchWriteback" => actorOf( new fileBatchWriteback( config, control ) )
        case "amqpBatchWriteback" =>  actorOf( new amqpBatchWriteback( config, control, amqpConnection._2 ) )
        case (e:String) => throw (new ClassNotFound(e))
      }
  }

  def newAmqpConnection(config:Config) = {
    import com.rabbitmq.client._
    val factory= new ConnectionFactory() // This will come from the config
    /* factory.setUri() doesn't work, not sure why
     (config getOption "uri") match {
      case Some(uri:String) => //factory.setUri(uri)
      case _ => ()
    } */
    val l= List( ("user", factory.setUsername(_)),
                 ("password", factory.setPassword(_)),
                 ("host", factory.setHost(_)),
                 ("port", ( (s:String) => factory.setPort(s.toInt))),
                 ("virtualHost", factory.setVirtualHost(_))
               )
    l foreach ( x => if(config exists x._1) { x._2(config(x._1)) } )

    val conn= factory.newConnection()
    val chan= conn.createChannel()
    println(conn)
    println(chan)
    (conn, chan)
  }

}