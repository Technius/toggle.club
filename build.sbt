name := """toggleclub"""

version := "1.0-SNAPSHOT"

lazy val sharedSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).aggregate(client, server)

lazy val common =
  crossProject.crossType(CrossType.Pure).in(file("common"))
    .settings(sharedSettings: _*)
    .settings(
      libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.3.6"
    )
    .jsConfigure(_.enablePlugins(ScalaJSPlay))

lazy val commonJs = common.js

lazy val commonJvm = common.jvm

lazy val server =
  (project in file("server"))
    .settings(sharedSettings: _*)
    .settings(
      name := "toggleclub",
      libraryDependencies ++= Seq(
        jdbc,
        cache,
        "org.webjars" %% "webjars-play" % "2.4.0-1",
        "org.webjars.npm" % "mithril" % "0.2.0",
        "org.webjars.npm" % "purecss" % "0.6.0",
        "org.webjars" % "font-awesome" % "4.4.0"
      ),
      routesGenerator := InjectedRoutesGenerator,
      Concat.groups := Seq(
        "js/app.js" -> group(((sourceDirectory in Assets).value / "js") ** "*.js")
      ),
      scalaJSProjects := Seq(client),
      pipelineStages := Seq(scalaJSProd, concat, uglify, digest, gzip),
      pipelineStages in Assets := Seq(concat),
      UglifyKeys.enclose in uglify := true,
      dockerRepository := Some("technius"),
      packageName in Docker := "toggle.club",
      dockerUpdateLatest := true,
      maintainer in Docker := "Bryan Tan <techniux@gmail.com>",
      dockerBaseImage := "java:8-jre",
      dockerExposedPorts := Seq(9000),
      dockerEntrypoint in Docker := Seq("sh", "-c", "bin/toggleclub")
    )
    .enablePlugins(PlayScala)
    .aggregate(client)
    .dependsOn(commonJvm)

lazy val client =
  (project in file("client"))
    .settings(sharedSettings: _*)
    .settings(
      name := "toggleclub-client",
      resolvers += Resolver.sonatypeRepo("snapshots"),
      libraryDependencies ++= Seq(
        "co.technius" %%% "scalajs-mithril" % "0.1.0-SNAPSHOT"
      )
    )
    .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
    .dependsOn(commonJs)
