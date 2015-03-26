name := "Hortonworks Assembly Framework UI"

version := "1.0"

// webapp task
resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
  val webappBase = base / "src" / "main" / "webapp"
  for {
    (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
  } yield {
    Sync.copy(from, to)
    to
  }
}

// spray-resolver setting
seq(Revolver.settings: _*)

// Watch webapp files
watchSources <++= baseDirectory map { path => ((path / "src" / "main" / "webapp") ** "*").get }

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Hortonworks Releases" at "http://repo.hortonworks.com/content/repositories/releases/"

libraryDependencies ++= {
  val akkaV = "2.3.0"
  val sprayV = "1.3.1"
  Seq(
    "org.java-websocket"  %   "Java-WebSocket" % "1.3.0",
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-httpx" % sprayV,
    "io.spray"            %%  "spray-json" % "1.2.6",
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test",
    "org.slf4j" 		  %   "slf4j-simple"  % "1.6.4",
    "org.apache.activemq" %   "activemq-core" % "5.5.1",
    "org.apache.solr" 	  %	  "solr-solrj"	  % "4.7.2" exclude("org.apache.zookeeper", "zookeeper"),
    "org.apache.httpcomponents" % "httpclient" % "4.3.1",
    "org.apache.hbase"	 %	  "hbase-client"  % "0.98.0.2.1.1.0-385-hadoop2",
    "org.apache.hbase"	 %	  "hbase-common"  % "0.98.0.2.1.1.0-385-hadoop2",
    "org.apache.hadoop"  %    "hadoop-common" % "2.4.0.2.1.5.0-695",
    "org.apache.hadoop"  %	  "hadoop-mapreduce-client-app" % "2.4.0.2.1.5.0-695",
    "com.sclasen" %% "akka-kafka" % "0.0.6" % "compile",
	"com.typesafe.akka" %% "akka-slf4j" % "2.3.2" % "compile"
  )
}