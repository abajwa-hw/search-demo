package com.hortonworks.apps

import com.hortonworks.demo.framework.api.ApiApp
import com.hortonworks.demo.framework.api.websocket.Broadcast
import akka.actor.Props
import com.hortonworks.demo.framework.services.KafkaMessagePrinter
import com.hortonworks.demo.framework.services.KafkaBroadcastEmitter

object LogManagementApplication extends ApiApp {
  addSolrActor("access_logs")
  addHbaseActor("access_logs")

  // Link the Kafka Actor to the WebSocket Broadcaster via the KafkaBradcastEmitter
  system.actorOf(Props[KafkaBroadcastEmitter], "bcast")
  addKafkaActor("access_logs_alerts", system.actorSelection("user/bcast"))
  
  startServer

//  var increment = 0
//  while (true) {
//    increment += 1
//    system.actorSelection("user/messageRouter") ! Broadcast(increment.toString)
//    Thread.sleep(5000)
//  }
}