package com.hortonworks.demo.framework.services

import scala.collection.JavaConverters._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocument
import org.apache.solr.common.SolrDocumentList
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.json.DefaultJsonProtocol._
import com.hortonworks.demo.framework.api.ApiActor
import spray.routing.Route
import scala.concurrent.Await
import akka.pattern.AskableActorRef
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import akka.actor.ActorSystem
import akka.event.Logging
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import QueryResultJson._
import org.apache.solr.client.solrj.SolrRequest
import org.apache.solr.client.solrj.request.LukeRequest
import org.apache.http.client.methods.HttpGet
import com.hortonworks.demo.framework.api.ApiConfiguration
import scala.collection.mutable.MutableList

object SolrServiceActor {
  def props(core: String): Props = Props(new SolrServiceActor(core))
  val ActorNamePrefix: String = "solr-"
}

class SolrServiceActor(core: String) extends Actor with ApiActor {
  val logger = Logging(context.system, this)

  protected var solrQuery = new SolrQuery()
  protected var id: String = "id"

  def getActorNamePrefix: String = { SolrServiceActor.ActorNamePrefix }
  def url = { ApiConfiguration.getConfig.getString("app.urls.solr") + core }
  implicit val solrTimeout = Timeout(30 seconds)
  val server = new HttpSolrServer(url)

  protected def responseToMap(response: QueryResponse): QueryResult = {
    def toSeq(docList: SolrDocumentList): IndexedSeq[JsValue] = {
      (for (i <- 0 to docList.size() - 1) yield {
        val doc = docList.get(i)
        val map = doc.getFieldNames().asScala.map(key => key -> doc.getFieldValue(key).toString).toMap
        map.toJson
      })
    }
    val facetMap = scala.collection.mutable.Map[String, List[Tuple2[String, Long]]]()
    if (null != response.getFacetFields()) {
      val facets = response.getFacetFields().asScala.toSeq
      for (field <- facets) {
        val facetList = MutableList[Tuple2[String, Long]]()
        for (values <- field.getValues().asScala) {
          facetList += Tuple2(values.getName(), values.getCount())
        }
        facetMap ++= Map(field.getName() -> facetList.toList)
      }
    }
    val highlightListMap = scala.collection.mutable.Map[String, List[List[String]]]()
    if (null != response.getHighlighting()) {
      val highlights = response.getHighlighting().asScala;
      for (document <- highlights) {
        val highlightList = MutableList[List[String]]()
        for (fields <- document._2.asScala.toMap) {
            highlightList += fields._2.asScala.toList
        }
        highlightListMap(document._1) = highlightList.toList
      }
    }
    QueryResult(response.getResults().getNumFound(), toSeq(response.getResults()).toList, response.getResults().getStart(), facetMap.toMap, highlightListMap.toMap)
  }

  def doSearch(query: String, filter: String, start: Integer, facetField: String, highlightField: String): QueryResult = {
    val solrQuery = new SolrQuery
    solrQuery.setQuery(query)
    if (start > 0) solrQuery.setStart(start)
    if (facetField != "") {
      solrQuery.setFacet(true)
      solrQuery.addFacetField(facetField)
      solrQuery.addHighlightField(highlightField)
      solrQuery.setHighlightFragsize(200);
      solrQuery.setHighlightSnippets(100);
    }
    responseToMap(server.query(solrQuery))
  }

  def getFields(): Unit = {
    //val response = server.getHttpClient().execute(new HttpGet("http://bimota.hortonworks.local:8983/solr/" + core + "/schema/fields"))
    println(server.getBaseURL())
  }

  def receive = {
    case GetURL => { logger.info("URL: " + url); sender() ! url }
    case GetCore => { logger.info("Core: " + core); sender() ! core }
    case GetFields => { logger.info("Retrieving Schema"); getFields }
    case q: QSearch => { sender() ! doSearch(q.query, null, q.start, q.facetField, null).toJson.toString }
    case hq: HQSearch => { sender() ! doSearch(hq.query, null, hq.start, hq.facetField, hq.hightlightField).toJson.toString }
    case fq: FQSearch => { sender() ! doSearch(fq.query, fq.filterQuery, fq.start, null, null).toJson.toString }
    case x => logger.warning("Received unknown message from sender: " + sender(), x)
  }
}

case class GetURL
case class GetCore
case class QSearch(query: String, start: Integer, facetField: String)
case class HQSearch(query: String, start: Integer, facetField: String, hightlightField: String)
case class FQSearch(query: String, filterQuery: String, start: Integer, facetField: String)
case class QueryResult(numFound: Long, results: List[JsValue], start: Long, facets: Map[String, List[Tuple2[String, Long]]], highlights: Map[String, List[List[String]]])
case class GetFields
