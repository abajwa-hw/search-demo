package com.hortonworks.demo.framework.api.websocket

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.Tcp
import akka.io.Tcp.Write
import akka.util.ByteString
import org.java_websocket.WebSocket

class SocketActor(val connection : ActorRef) extends Actor with ActorLogging {
  val webSocketActor = context.actorOf(Props[WebSocketActor])
  webSocketActor ! Start(null)

  override def receive = {
    case Tcp.Received(data) => webSocketActor ! Test
    case Tcp.PeerClosed => stop()
    case Tcp.ErrorClosed => stop()
    case Tcp.Closed => stop()
    case Tcp.ConfirmedClosed => stop()
    case Tcp.Aborted => stop()
  }
  private def stop() {
    webSocketActor ! Stop
    context stop self
  }
}