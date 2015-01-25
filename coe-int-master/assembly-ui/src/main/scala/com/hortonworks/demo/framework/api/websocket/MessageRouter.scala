package com.hortonworks.demo.framework.api.websocket

import akka.actor.{ Actor, ActorLogging }
import scala.collection._
import org.java_websocket.WebSocket

class MessageRouter extends Actor with ActorLogging {
  import com.hortonworks.demo.framework.api.websocket.ApiWebSocketServer._

  val clients = mutable.ListBuffer[WebSocket]()
  override def receive = {
    case Open(ws, hs) => {
      clients += ws
      log.info("WebSocket client connected from IP: {}", ws.getRemoteSocketAddress())
    }
    case Close(ws, code, reason, ext) => self ! Unregister(ws)
    case Error(ws, ex) => self ! Unregister(ws)
    case Message(ws, msg) => {
      log.info("Recieved message: {} from IP: {}", msg, ws.getRemoteSocketAddress())
    }
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("unregister monitor")
        clients -= ws
      }
    }
    case Broadcast(message) => {
      for (client <- clients) {
        client.send(message)
      }
    }
  }
}

case class Unregister(ws: WebSocket)
case class Broadcast(message: String)