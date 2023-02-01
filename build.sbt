import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings


lazy val scalaCompilerOptions = Seq(
    "-Xfatal-warnings",
    "-Xlint:-missing-interpolator,_",
    "-Xlint:adapted-args",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    // required in place of silencer plugin
    "-Wconf:cat=unused-imports&src=html/.*:s",
    "-Wconf:src=routes/.*:s"
)

lazy val microservice = Project("direct-debit-update-email-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(DefaultBuildSettings.scalaSettings: _*)
  .settings(DefaultBuildSettings.defaultSettings(): _*)
  .settings(
    majorVersion        := 0,
    scalaVersion        := "2.13.10",
    PlayKeys.playDefaultPort := 10801,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions ++= scalaCompilerOptions,
    pipelineStages := Seq(gzip)
  )
  .settings(
      commands += Command.command("runTestOnly") { state =>
          state.globalLogging.full.info("running play using 'testOnlyDoNotUseInAppConf' routes...")
          s"""set javaOptions += "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
            "run" ::
            s"""set javaOptions -= "-Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"""" ::
            state
      }
  )
  .settings(TwirlKeys.templateImports := Seq.empty)
  .settings(ScalariformSettings.scalariformSettings: _*)
  .settings(WartRemoverSettings.wartRemoverSettings: _*)
  .settings(ScoverageSettings.scoverageSettings: _*)
  .settings(
      Compile / doc / scalacOptions := Seq() //this will allow to have warnings in `doc` task
  )
  .settings(SbtUpdatesSettings.sbtUpdatesSettings: _*)
