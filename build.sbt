name := """waitingclub"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.lihaoyi" %% "upickle" % "0.3.6",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "mithril" % "0.2.0",
  "org.webjars.npm" % "purecss" % "0.6.0"
)

Concat.groups := Seq(
  "js/app.js" -> group(((sourceDirectory in Assets).value / "js") ** "*.js")
)

pipelineStages := Seq(concat, uglify, digest, gzip)

pipelineStages in Assets := Seq(concat)

UglifyKeys.enclose in uglify := true

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
