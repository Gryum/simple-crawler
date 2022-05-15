ThisBuild / scalaVersion := "2.13.8"

ThisBuild / organization := "io.github.gryum.crawler"

ThisBuild / scapegoatVersion := "1.4.14"

lazy val root = (project in file("."))
  .settings(
    name := "simple-crawler",
    libraryDependencies ++= loggingDeps ++ akkaDeps ++ Seq(configDep, pureConfig, jsoup) ++ testDeps
  )

//deps

val akkaHttpVersion = "10.2.9"
val akkaVersion = "2.6.19"
val logbackVersion = "1.2.11"
val configVersion = "1.4.2"
val pureConfigVersion = "0.17.1"
val scalaLoggingVersion = "3.9.4"
val testVersion = "3.0.5"
val akkaStreamVersion = "2.5.26"
val scalaTestVersion = "3.2.12"
val jsoupVersion = "1.14.3"

val akkaActor = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaSlf4 = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val streamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
val configDep = "com.typesafe" % "config" % configVersion
val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
val jsoup = "org.jsoup" % "jsoup" % jsoupVersion

val akkaDeps = Seq(akkaActor, akkaStream, akkaSlf4, akkaHttp, akkaHttpSprayJson)
val loggingDeps = Seq(logbackClassic, akkaSlf4, scalaLogging)
val testDeps = Seq(scalaTest, akkaTestKit, streamTestKit) map (_ % Test)
