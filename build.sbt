import uk.gov.hmrc.DefaultBuildSettings

lazy val scalaCompilerOptions = Seq(
    "-Xfatal-warnings",
    "-Wvalue-discard",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-language:strictEquality",
    // required in place of silencer plugin
    "-Wconf:msg=unused-imports&src=html/.*:s",
    "-Wconf:src=routes/.*:s"
)

lazy val microservice = Project("direct-debit-update-email-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(DefaultBuildSettings.scalaSettings *)
  .settings(DefaultBuildSettings.defaultSettings() *)
  .settings(
    majorVersion        := 1,
    scalaVersion        := "3.5.1",
    PlayKeys.playDefaultPort := 10801,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
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
  .settings(WartRemoverSettings.wartRemoverSettings *)
  .settings(ScoverageSettings.scoverageSettings *)
  .settings(
      Compile / doc / scalacOptions := Seq() //this will allow to have warnings in `doc` task
  )
  .settings(SbtUpdatesSettings.sbtUpdatesSettings *)
  .settings(scalafmtOnCompile := true)

