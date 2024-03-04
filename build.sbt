import uk.gov.hmrc.DefaultBuildSettings.{integrationTestSettings, targetJvm}

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
    scalaVersion        := "2.13.12",
    targetJvm           := "jvm-11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
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
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
