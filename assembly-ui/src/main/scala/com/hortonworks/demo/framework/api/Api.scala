package com.hortonworks.demo.framework.api

import scala.concurrent.duration.DurationInt
import com.hortonworks.demo.framework.services.SolrServiceActor
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.Tcp
import akka.pattern.AskableActorRef
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import com.hortonworks.demo.framework.services.HbaseServiceActor
import akka.actor.Props
import java.net.InetSocketAddress
import com.hortonworks.demo.framework.api.websocket.ApiWebSocketServer
import com.hortonworks.demo.framework.api.websocket.WebSocketActor
import com.hortonworks.demo.framework.api.websocket.SocketService
import java.net.InetSocketAddress
import com.hortonworks.demo.framework.api.websocket.MessageRouter
import com.hortonworks.demo.framework.services.KafkaMessageServiceActor
import akka.actor.ActorSelection

trait Api {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("app")

  val coreHttpServiceActor: ActorRef = null

  // List of Actors participating in this Api
  var apiActors: Map[ApiType.Value, String] = Map()

  def addApiActor(actorType: ApiType.Value, apiActor: AskableActorRef, name: String): Unit = {
    apiActors += (actorType -> name)
  }

  def addSolrActor(core: String): Unit = {
    val name = SolrServiceActor.ActorNamePrefix + core
    addApiActor(ApiType.Solr, system.actorOf(SolrServiceActor.props(core), name), name)
  }

  def addHbaseActor(table: String): Unit = {
    val name = HbaseServiceActor.ActorNamePrefix + table
    addApiActor(ApiType.HBase, system.actorOf(HbaseServiceActor.props(table), name), name)
  }

  def addHbaseActor(table: String, typeMap: Map[String, String]) {
    val name = HbaseServiceActor.ActorNamePrefix + table
    addApiActor(ApiType.HBase, system.actorOf(HbaseServiceActor.props(table, typeMap), name), name)
  }
  
  def addKafkaActor(topic: String, actor:ActorSelection) {
    val name = KafkaMessageServiceActor.ActorNamePrefix + topic
    addApiActor(ApiType.Kafka, system.actorOf(KafkaMessageServiceActor.props(topic, actor), name), name)
  }

  def startServer = {
    implicit val timeout = Timeout(10 seconds)
    // create and start our service actors
    val coreHttpServiceActor = system.actorOf(CoreHttpServiceActor.props(apiActors), "coreHttpService")
    val server = system.actorOf(Props[SocketService], "webSocketServer")
    val messageRouter = system.actorOf(Props[MessageRouter], "messageRouter")
    val apiWebSocketServer = new ApiWebSocketServer(ApiConfiguration.portWs, messageRouter)
    apiWebSocketServer.start
    sys.addShutdownHook({system.shutdown;apiWebSocketServer.stop})
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Tcp) ! Tcp.Bind(server, new InetSocketAddress(ApiConfiguration.host, ApiConfiguration.portTcp))
    IO(Http) ! Http.Bind(coreHttpServiceActor, interface = ApiConfiguration.host, port = ApiConfiguration.portHttp)
  }
}

object ApiConfiguration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("app.host")
  val portHttp = config.getInt("app.ports.http")
  val portTcp = config.getInt("app.ports.tcp")
  val portWs = config.getInt("app.ports.ws")

  def getConfig(): Config = { config }
}

class ApiType {}

object ApiType extends Enumeration {
  val Solr = Value
  val HBase = Value
  val Kafka = Value
}