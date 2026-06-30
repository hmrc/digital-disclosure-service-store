import uk.gov.hmrc.DefaultBuildSettings.{integrationTestSettings}

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project("digital-disclosure-service-store", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion        := 0,
    scalaVersion        := "3.3.7",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    excludeDependencies += ExclusionRule("org.lz4", "lz4-java"),
    libraryDependencySchemes ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    ),
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    PlayKeys.playDefaultPort := 15005
  )
  .settings(scoverageSettings : _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(CodeCoverageSettings.settings: _*)
