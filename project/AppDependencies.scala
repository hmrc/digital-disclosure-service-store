import sbt._

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "10.5.0"
  private val hmrcMongoVersion = "2.12.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-backend-$playVersion"    % bootstrapVersion exclude("org.apache.commons", "commons-lang3"),
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-$playVersion"           % hmrcMongoVersion,
    "uk.gov.hmrc"            %% s"internal-auth-client-$playVersion" % "4.3.0",
    "uk.gov.hmrc"            %% s"crypto-json-$playVersion"          % "8.4.0",
    "org.apache.commons"    % "commons-lang3"                          % "3.18.0",
    "ch.qos.logback"        % "logback-core"                           % "1.5.27",
    "ch.qos.logback"        % "logback-classic"                        % "1.5.27",
    "at.yawk.lz4"           %  "lz4-java"                              % "1.10.3",
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.scalatestplus" %% "mockito-3-4"                   % "3.2.10.0"
  ).map(_ % "test, it")
}
