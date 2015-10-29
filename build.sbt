name := """toggleclub"""

version := "1.0-SNAPSHOT"

lazy val sharedSettings = Seq(
  scalaVersion := "2.11.6"
)

lazy val root = (project in file(".")).aggregate(server)

lazy val server =
  (project in file("server"))
    .settings(sharedSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        jdbc,
        cache,
        "com.lihaoyi" %% "upickle" % "0.3.6",
        "org.webjars" %% "webjars-play" % "2.4.0-1",
        "org.webjars.npm" % "mithril" % "0.2.0",
        "org.webjars.npm" % "purecss" % "0.6.0",
        "org.webjars" % "font-awesome" % "4.4.0"
      ),
      routesGenerator := InjectedRoutesGenerator,
      Concat.groups := Seq(
        "js/app.js" -> group(((sourceDirectory in Assets).value / "js") ** "*.js")
      ),
      pipelineStages := Seq(concat, uglify, digest, gzip),
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
