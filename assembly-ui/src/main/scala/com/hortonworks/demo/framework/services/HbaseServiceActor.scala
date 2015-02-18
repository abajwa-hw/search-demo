package com.hortonworks.demo.framework.services

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.HConnection
import com.hortonworks.demo.framework.api.ApiActor
import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import org.apache.hadoop.hbase.client.HBaseAdmin
import com.hortonworks.demo.framework.api.ApiConfiguration
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.HTableInterface
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.HConnectionManager
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.Get
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.json.DefaultJsonProtocol._
import org.apache.hadoop.hbase.client.Scan
import scala.collection.JavaConversions._
import akka.actor.ActorLogging

object HbaseServiceActor {
  def props(table: String): Props = Props(new HbaseServiceActor(table))
  def props(table: String, typeMap: Map[String, String]): Props = Props(new HbaseServiceActor(table, typeMap))
  val ActorNamePrefix: String = "hbase-"
}
class HbaseServiceActor extends Actor with ApiActor with ActorLogging {
  var connection: HConnection = null
  val config: Configuration = HBaseConfiguration.create()
  var tableInterface: HTableInterface = null
  var typeMap: Map[String, String] = Map()

  def this(table: String) = {
    this()
    config.set("hbase.zookeeper.quorum", ApiConfiguration.getConfig.getString("app.hbase.zk-quorum"))
    config.set("zookeeper.znode.parent", ApiConfiguration.getConfig.getString("app.hbase.zk-znode-parent"))
    HBaseAdmin.checkHBaseAvailable(config)
    connection = HConnectionManager.createConnection(config);
    tableInterface = connection.getTable(Bytes.toBytes(table))
  }

  def this(table: String, typeMap: Map[String, String]) {
    this(table)
    this.typeMap = typeMap
  }

  def getActorNamePrefix: String = { HbaseServiceActor.ActorNamePrefix }

  def get(key: String): Option[Result] = {
    val result: Result = tableInterface.get(new Get(Bytes.toBytes(key)))
    if (result != null)
      return Some(result)
    else
      return None
  }

  def scan(startRow: String, stopRow: String): Option[Array[Result]] = {
    log.info("Starting Scan with startRow: " + startRow + " and stopRow: " + stopRow)
    val scan: Scan = new Scan()
    if (startRow != null) scan.setStartRow(Bytes.toBytes(startRow))
    if (stopRow != null) scan.setStopRow(Bytes.toBytes(stopRow))
    val scanResults = tableInterface.getScanner(scan)
    if (scanResults != null)
      return Some(scanResults.iterator().toArray)
    else
      return None
  }

  def put(key: String, cf: String, qual: String, value: String): Unit = {
    val put: Put = new Put(key.getBytes());
    Bytes.toBytes("")
    put.add(cf.getBytes(), qual.getBytes(), value.getBytes());
    tableInterface.put(put);
  }

  def receive() = {
    case GetRow(rowKey) => sender() ! {
      get(rowKey) match {
        case Some(result) => { result.toJson(new HbaseServiceRowWriter(typeMap)).toString }
        case None => { "" }
      }
    }
    case ScanTable(startRow, stopRow) => sender() ! {
      scan(startRow, stopRow) match {
        case Some(results) => { results.toJson(new HbaseServiceScanWriter(typeMap)).toString }
        case None => ""
      }
    }
  }
}

case class GetRow(rowKey: String)
case class ScanTable(startRow: String, stopRow: String)