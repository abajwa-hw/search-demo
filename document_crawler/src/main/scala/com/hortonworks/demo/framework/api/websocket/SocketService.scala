package com.hortonworks.demo.framework.api.websocket

import akka.actor.{Actor, ActorLogging, Props}
import akka.io.Tcp

class SocketService extends Actor with ActorLogging {
  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self
    case Tcp.Connected(remote, local) =>
      sender ! Tcp.Register(context.actorOf(Props(classOf[SocketActor], sender)))
  }
}