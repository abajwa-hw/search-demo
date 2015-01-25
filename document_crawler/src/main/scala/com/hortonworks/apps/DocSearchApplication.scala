package com.hortonworks.apps

import com.hortonworks.demo.framework.api.ApiApp
import com.hortonworks.demo.framework.api.websocket.Broadcast
import akka.actor.Props
import com.hortonworks.demo.framework.services.KafkaMessagePrinter
import com.hortonworks.demo.framework.services.KafkaBroadcastEmitter

object DocSearchApplication extends ApiApp {
  addSolrActor("rawdocs")  
  startServer
}