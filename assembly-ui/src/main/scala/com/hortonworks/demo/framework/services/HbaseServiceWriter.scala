package com.hortonworks.demo.framework.services

import scala.collection.JavaConverters._
import spray.json.JsValue
import spray.json.JsArray
import spray.json.RootJsonFormat
import org.apache.hadoop.hbase.client.Result
import spray.json.JsNumber
import spray.json.JsString
import spray.json.DefaultJsonProtocol
import spray.json.JsObject
import spray.json.DeserializationException
import org.apache.hadoop.hbase.util.Bytes
import com.hortonworks.demo.framework.services.HbaseServiceActor._
import akka.actor.ActorSystem
import spray.json.JsonWriter
import spray.json._
import DefaultJsonProtocol._

object HbaseServiceWriterRowUtil {
  def resolveRow(result: Result, typeMap: Map[String, String]): JsObject = {
    val resultMap = result.getMap().asScala
    val fields = new collection.mutable.ListBuffer[(String, JsValue)]
    fields ++= Map("rowkey" -> JsString(Bytes.toString(result.getRow())))
    if (result != null && result.getMap() != null) {
      for ((family, quals) <- result.getMap().asScala) {
        for (qual <- quals.asScala) {
          val qualMap = collection.mutable.Map[java.lang.Long, Array[Byte]]()
          for (version <- qual._2.asScala) {
            qualMap += (version._1 -> version._2)
          }
          val famAndQual = Bytes.toString(family) + "_" + Bytes.toString(qual._1)
          typeMap.get(famAndQual) match {
            case Some(result) => result match {
              case "String" => fields ++= Map(famAndQual -> JsString(Bytes.toString(qualMap.head._2)))
              case "Long" => fields ++= Map(famAndQual -> JsNumber(Bytes.toLong(qualMap.head._2)))
            }
            case None => fields ++= Map(famAndQual -> JsString(Bytes.toString(qualMap.head._2)))
          }
        }
      }
      return JsObject(fields: _*)
    } else {
      return JsObject()
    }
  }
}

class HbaseServiceRowWriter(typeMap: Map[String, String]) extends JsonWriter[Result] {
  def write(result: Result) = HbaseServiceWriterRowUtil.resolveRow(result, typeMap)
}

class HbaseServiceScanWriter(typeMap: Map[String, String]) extends JsonWriter[Array[Result]] {
  def write(results: Array[Result]) = {
    var resultArray = Array[JsObject]()
    for (result <- results) {
      resultArray = resultArray :+ HbaseServiceWriterRowUtil.resolveRow(result, typeMap)
    }
    resultArray.toJson
  }
}