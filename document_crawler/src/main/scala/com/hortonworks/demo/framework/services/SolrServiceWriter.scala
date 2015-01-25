package com.hortonworks.demo.framework.services;

import spray.json.DefaultJsonProtocol._

object QueryResultJson {
  implicit val queryResultFormat = jsonFormat5(QueryResult)
}  