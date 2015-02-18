package com.hortonworks.demo.framework.api.websocket
import akka.actor.{ Actor, ActorLogging, PoisonPill }
import org.java_websocket.WebSocket
import java.util.UUID

object WebSocketActor {

}
class WebSocketActor extends Actor with ActorLogging {
  var client: WebSocket = _
  override def receive = {
    case Stop => {
      self ! PoisonPill
    }
    case Start(ws) => {
      client = ws
      if (null != client) client.send("OK")
    }
    case Test => {
      log.info("here!! client is null? {}", (if(null==client)":("else":)"))
      if (null != client) client.send("OK")
    }
  }
}

case class Start(ws: WebSocket)
case class Stop
case class Test