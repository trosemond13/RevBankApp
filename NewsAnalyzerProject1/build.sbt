name := "NewsAnalyzerProject1"

version := "0.1"

scalaVersion := "2.11.12"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "2.3.0"
libraryDependencies += "com.outr" %% "hasher" % "1.2.2"

val akkaVersion = "2.5.8"
val akkaHttpVersion = "10.0.15"
libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor"  % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion

libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"

libraryDependencies += "com.lihaoyi" %% "upickle" % "0.7.1"
libraryDependencies += "com.lihaoyi" %% "ujson" % "1.4.3"
libraryDependencies +="com.lihaoyi" %% "os-lib" % "0.8.0"