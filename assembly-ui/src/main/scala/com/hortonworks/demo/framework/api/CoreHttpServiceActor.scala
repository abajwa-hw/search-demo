package com.hortonworks.demo.framework.api

import akka.actor.Actor
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.Route
import akka.actor.Props
import akka.event.Logging
import scala.collection.mutable.MutableList
import com.hortonworks.demo.framework.services.SolrServiceActor
import akka.pattern.AskableActorRef
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorSystem
import com.hortonworks.demo.framework.services.GetURL
import com.hortonworks.demo.framework.services.QSearch
import akka.actor.ActorSelection
import com.hortonworks.demo.framework.services.GetFields
import com.hortonworks.demo.framework.services.HbaseServiceActor
import com.hortonworks.demo.framework.services.GetRow
import com.hortonworks.demo.framework.services.ScanTable

/**
 * @author paul
 *
 * Main API integration point.  If there are additional routes that need to be
 * exposed for your application, include the trait, and make sure the routes
 * are added using ~ to the runRoute function.
 */
object CoreHttpServiceActor {
  def props(apiActors: Map[ApiType.Value, String]): Props = Props(new CoreHttpServiceActor(apiActors))
}

class CoreHttpServiceActor(apiActorsFromConfig: Map[ApiType.Value, String]) extends Actor with CoreHttpService {
  val logger = Logging(context.system, this)

  implicit val apiActors = apiActorsFromConfig

  def this() { this(Map[ApiType.Value, String]()) }

  def actorRefFactory = context

  def receive = runRoute(constructFinalRoutes)
}

trait CoreHttpService extends HttpService {
  implicit val apiActors: Map[ApiType.Value, String];

  val baseRoutes = {
    pathPrefix("css") {
      getFromResourceDirectory("webapp/app/css/")
    } ~
      pathPrefix("js") {
        getFromResourceDirectory("webapp/app/js/")
      } ~
      pathPrefix("bower_components") {
        getFromResourceDirectory("webapp/app/bower_components/")
      } ~
      pathPrefix("partials") {
        getFromResourceDirectory("webapp/app/partials/")
      } ~
      pathPrefix("img") {
        getFromResourceDirectory("webapp/app/img")
      } ~
      path("api") {
        get {
          complete {
            "OK"
          }
        }
      } ~
      path("") {
        getFromResource("webapp/app/index.html")
      }
  }

  def constructFinalRoutes: Route = {
    constructRoutesFromApiClasses(baseRoutes)
  }

  def constructRoutesFromApiClasses(routes: Route): Route = {
    implicit val timeout = Timeout(10 seconds)
    var newRoutes = routes
    for (apiType <- apiActors.keys) {
      apiActors.get(apiType) match {
        case Some(name) => {
          if (apiType == ApiType.Solr) {
            val coreName = name.split(SolrServiceActor.ActorNamePrefix)(1)
            newRoutes = newRoutes ~
              pathPrefix("solr") {
                pathPrefix(coreName) {
                  // Define your global Solr path handlers here
                  path("getUrl") {
                    complete {
                      def getUrl = {
                        val actor: ActorSelection = actorRefFactory.actorSelection("../" + name)
                        val request = actor ? GetURL
                        Await.result(request, timeout.duration).asInstanceOf[String]
                      }
                      getUrl
                    }
                  } ~
                    path("getFields") {
                      complete {
                        def getFields = {
                          val actor: ActorSelection = actorRefFactory.actorSelection("../" + name)
                          val request = actor ? GetFields
                          Await.result(request, timeout.duration).asInstanceOf[String]
                        }
                        getFields
                      }
                    } ~
                    path("search") {
                      get {
                        parameters("query", "start".as[Int] ? 0, "facet.field" ? "") { (query, start, facetField) =>
                          complete {
                            def search = {
                              val request = actorRefFactory.actorSelection("../" + name) ? QSearch(query, start, facetField)
                              Await.result(request, timeout.duration).asInstanceOf[String]
                            }
                            search
                          }
                        }
                      }
                    }
                }
              }
          }
          if (apiType == ApiType.HBase) {
            val tableName = name.split(HbaseServiceActor.ActorNamePrefix)(1)
            newRoutes = newRoutes ~
              pathPrefix("hbase") {
                pathPrefix(tableName) {
                  // Define your global HBase path handlers here
                  path("get") {
                    get {
                      parameters("rowKey") { (rowKey) =>
                        complete {
                          def getRow = {
                            val request = actorRefFactory.actorSelection("../" + name) ? GetRow(rowKey)
                            Await.result(request, timeout.duration).asInstanceOf[String]
                          }
                          getRow
                        }
                      }
                    }
                  } ~
                  path("scan") {
                    get {
                      parameters("startRow" ? "", "endRow" ? "") { (startRow,endRow) =>
                        complete {
                          val request = actorRefFactory.actorSelection("../" + name) ? ScanTable(startRow, endRow)
                          Await.result(request, timeout.duration).asInstanceOf[String]
                        }
                      }
                    }
                  }
                }
              }
          }
        }
        case None => rejectEmptyResponse
      }
    }
    newRoutes
  }
}
