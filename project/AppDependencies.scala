import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.13.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"                    % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"                            % "6.3.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"                 % "1.12.0-play-28",
    "com.beachape"      %% "enumeratum-play"                               % "1.7.2",
    "org.typelevel"     %% "cats-core"                                     % "2.9.0",
    "uk.gov.hmrc"       %% "direct-debit-update-email-backend-cor-journey" % "0.2.0",
    "uk.gov.hmrc"       %% "emailaddress"                                  % "3.7.0"

  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion ,
    "org.jsoup"               %  "jsoup"                      % "1.15.3"
  ).map(_ % Test)
}
