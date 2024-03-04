import sbt._

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-backend-$playVersion"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-$playVersion"           % hmrcMongoVersion,
    "uk.gov.hmrc"            %% s"internal-auth-client-$playVersion" % "1.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.scalatestplus" %% "mockito-3-4"                   % "3.2.10.0"
  ).map(_ % "test, it")
}
