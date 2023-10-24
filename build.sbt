ThisBuild / version := "0.6.0"

ThisBuild / scalaVersion := "2.12.15"

ThisBuild / organization := "io.github.simplifier-ag"

licenses := Seq(
  ("MIT", url("http://opensource.org/licenses/MIT"))
)

lazy val compileSettings = Seq(
  scalacOptions := Seq(
    "-unchecked", "-deprecation", "-feature", "-encoding", "utf8",
    "-Xmax-classfile-name", "100", "-Ypatmat-exhaust-depth", "off"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
)

lazy val root = (project in file("."))
  .settings(
    name := "simplifier-plugin-api",
    compileSettings,
    // PublishToMavenCentral.settings,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.typesafe.akka" %% "akka-actor" % akkaV withSources() withJavadoc(),
      "com.typesafe.akka" %% "akka-remote" % akkaV withSources() withJavadoc(),
      "com.typesafe.akka" %% "akka-slf4j" % akkaV withSources() withJavadoc(),
      "com.typesafe.akka" %% "akka-stream" % akkaV withSources() withJavadoc(),
      "com.typesafe.akka" %% "akka-http" % akkaHttpV withSources() withJavadoc(),
      "org.json4s"        %% "json4s-jackson" % json4sV withSources() withJavadoc(),
      "com.typesafe" % "config" % configV withSources() withJavadoc(),
      "net.databinder.dispatch" %% "dispatch-core" % "0.12.0" withSources() withJavadoc(),
      "commons-codec" % "commons-codec" % commonsCodecV withSources() withJavadoc(),
      "commons-io" % "commons-io" % commonsIoV,
      "org.slf4j" % "slf4j-api" % slf4jV withSources() withJavadoc(),
      "ch.qos.logback" % "logback-classic" % logbackV withSources() withJavadoc(),
      "com.roundeights" %% "hasher" % hasherV
    ),
    dependencyOverrides ++= akkaOverrides ++ netty3Overrides,
    assembly / test := {},
    assembly / aggregate := false,
    assembly / assemblyJarName := s"simplifier-plugin-api_${scalaBinaryVersion.value}-${version.value}.jar"
  )

lazy val akkaV = "2.6.20"
lazy val akkaHttpV = "10.2.10"
lazy val netty3V = "3.10.6.Final"
lazy val configV = "1.3.1"
lazy val json4sV = "4.0.6"
lazy val hasherV = "1.2.0"
lazy val slf4jV = "1.7.25"
lazy val logbackV = "1.2.3"

lazy val commonsIoV = "2.5"
lazy val commonsCodecV = "1.11"

lazy val netty3Overrides = Seq(
  "io.netty" % "netty" % netty3V
)

// Version overrides, resolving conflict between akka 2.5 and akka-http declaring dependency to akka 2.4 (although being compatible to 2.5)
lazy val akkaOverrides = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV
)


pomExtra := (
  <developers>
    <developer>
      <id>C-Schwemin</id>
      <name>Christoph Schwemin</name>
    </developer>
    <developer>
      <id>andreasheunisch</id>
      <name>Andreas Heunisch</name>
    </developer>
  </developers>
  )
