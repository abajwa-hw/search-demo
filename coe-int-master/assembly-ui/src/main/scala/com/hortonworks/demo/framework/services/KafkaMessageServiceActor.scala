package com.hortonworks.demo.framework.services

import java.util.Properties
import com.hortonworks.demo.framework.api.ApiActor
import com.hortonworks.demo.framework.api.ApiConfiguration
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.actor.ActorRef
import com.sclasen.akka.kafka.{ AkkaConsumer, AkkaConsumerProps, StreamFSM }
import kafka.serializer.DefaultDecoder
import akka.actor.ActorSelection
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import kafka.serializer.StringEncoder
import kafka.serializer.StringDecoder
import com.hortonworks.demo.framework.api.websocket.Broadcast

object KafkaMessageServiceActor {
  def props(topic: String, receiver: ActorSelection): Props = Props(new KafkaMessageServiceActor(topic, receiver))
  val ActorNamePrefix: String = "kafka-"
}
class KafkaMessageServiceActor extends Actor with ApiActor {
  val logger = Logging(context.system, this)
  implicit val timeout: Timeout = Timeout(7 seconds)

  def this(topic: String, receiver: ActorSelection) = {
    this()
    val consumerProps = AkkaConsumerProps.forSystem(
      system = this.context.system,
      zkConnect = ApiConfiguration.getConfig.getString("app.kafka.zk-quorum"),
      topic = ApiConfiguration.getConfig.getString("app.kafka.topic"),
      group = ApiConfiguration.getConfig.getString("app.kafka.consumer-group"),
      streams = ApiConfiguration.getConfig.getInt("app.kafka.partitions"),
      keyDecoder = new DefaultDecoder(),
      msgDecoder = new StringDecoder(),
      receiver = Await.result(receiver.resolveOne, timeout.duration))
    val consumer = new AkkaConsumer(consumerProps)
    consumer.start()
  }

  def getActorNamePrefix: String = { KafkaMessageServiceActor.ActorNamePrefix }

  def receive = {
    case x => logger.warning("Received unknown message from sender: " + sender(), x)
  }
}

class KafkaMessagePrinter extends Actor with ApiActor {
  val logger = Logging(context.system, this)

  def getActorNamePrefix: String = { KafkaMessageServiceActor.ActorNamePrefix }

  def receive = {
    case x: Any =>
      println(x)
      sender ! StreamFSM.Processed
  }
}

class KafkaBroadcastEmitter extends Actor with ApiActor {
  val logger = Logging(context.system, this)

  def getActorNamePrefix: String = { KafkaMessageServiceActor.ActorNamePrefix }

  def receive = {
    case x: Any =>
      logger.debug("Received Kafka Message Alert: " + x)
      context.system.actorSelection("user/messageRouter") ! Broadcast(x.toString)
      sender ! StreamFSM.Processed
  } 
}