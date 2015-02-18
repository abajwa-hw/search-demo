package com.hortonworks.demo.framework.api.websocket

import akka.actor.ActorRef
import java.net.InetSocketAddress
import org.java_websocket.WebSocket
import org.java_websocket.framing.CloseFrame
import org.java_websocket.server.WebSocketServer
import org.java_websocket.handshake.ClientHandshake
import scala.collection.mutable.Map

object ApiWebSocketServer {
  sealed trait ApiWebSocketServerMessage
  case class Message(ws : WebSocket, msg : String)
  	extends ApiWebSocketServerMessage
  case class Open(ws : WebSocket, hs : ClientHandshake)
  	extends ApiWebSocketServerMessage
  case class Close(ws : WebSocket, code : Int, reason : String, external : Boolean)
  	extends ApiWebSocketServerMessage
  case class Error(ws : WebSocket, ex : Exception)
  	extends ApiWebSocketServerMessage
}
class ApiWebSocketServer(val port : Int, messageRouter: ActorRef)
    extends WebSocketServer(new InetSocketAddress(port)) { 
  
  final override def onMessage(ws : WebSocket, msg : String) {
    if (null != ws) {
        messageRouter ! ApiWebSocketServer.Message(ws, msg)
    }
  }
  final override def onOpen(ws : WebSocket, hs : ClientHandshake) {
    if (null != ws) {
        messageRouter ! ApiWebSocketServer.Open(ws, hs)
    }
  }
  final override def onClose(ws : WebSocket, code : Int, reason : String, external : Boolean) {
    if (null != ws) {
        messageRouter ! ApiWebSocketServer.Close(ws, code, reason, external)
    }
  }
  final override def onError(ws : WebSocket, ex : Exception) {
    if (null != ws) {
        messageRouter ! ApiWebSocketServer.Error(ws, ex)
    }
  }
}