val circeVersion = "0.9.0-M2"

scalacOptions += "-Ypartial-unification"

lazy val server = (project in file("server"))
  .settings(commonSettings)
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % "1.1.1",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "org.typelevel" %% "cats-core" % "1.0.1",
      "net.debasishg" %% "redisclient" % "3.4",
      guice,
      specs2 % Test
    ),
    // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
    EclipseKeys.preTasks := Seq(compile in Compile)
  )
  .enablePlugins(PlayScala)
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    // scalaJSModuleKind := ModuleKind.CommonJSModule,
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3",
      "be.doeraene" %%% "scalajs-jquery" % "0.9.2",
      "org.querki" %%% "jquery-facade" % "1.2",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "com.github.japgolly.scalajs-react" %%% "core" % "1.1.1"
      // "eu.unicredit" %%% "paths-scala-js" % "0.4.5"
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % "15.6.1"
        / "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",
      "org.webjars.bower" % "react" % "15.6.1"
        / "react-dom.js"
        minified "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM",
      "org.webjars.bower" % "react" % "15.6.1"
        / "react-dom-server.js"
        minified "react-dom-server.min.js"
        dependsOn "react-dom.js"
        commonJSName "ReactDOMServer",
      "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js"
    )
    // "org.singlespaced" %%% "scalajs-d3" % "0.3.4"
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .
//).enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.12.2",
  organization := "markgrafendamm.de",
  libraryDependencies ++= Seq(
    "org.scala-graph" %% "graph-core" % "1.12.2",
    "org.scalactic" %% "scalactic" % "3.0.4",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State =>
  "project server" :: s
}
