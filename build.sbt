scalaVersion := "2.13.8"

name         := "venues-market"
organization := "io.reality"
version      := "1.0"

val AkkaVersion      = "2.6.8"
val AkkaHttpVersion  = "10.2.9"
val LogbackVersion   = "1.2.11"
val ScalaTestVersion = "3.2.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"     % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream"          % AkkaVersion,
  "com.typesafe.akka" %% "akka-http"            % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "ch.qos.logback"     % "logback-classic"      % LogbackVersion,
  "com.typesafe.akka" %% "akka-stream-testkit"  % AkkaVersion      % Test,
  "com.typesafe.akka" %% "akka-http-testkit"    % AkkaHttpVersion  % Test,
  "org.scalatest"     %% "scalatest"            % ScalaTestVersion % Test,
)
