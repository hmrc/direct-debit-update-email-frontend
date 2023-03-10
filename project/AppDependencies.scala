import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.14.0"

  val compile = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-28"                    % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc"                            % "6.8.0-play-28",
    "uk.gov.hmrc"     %% "play-conditional-form-mapping"                 % "1.12.0-play-28",
    "com.beachape"    %% "enumeratum-play"                               % "1.7.2",
    "org.typelevel"   %% "cats-core"                                     % "2.9.0",
    "uk.gov.hmrc"     %% "direct-debit-update-email-backend-cor-journey" % "0.6.0",
    "uk.gov.hmrc"     %% "payments-email-verification-cor"               % "0.2.0",
    "uk.gov.hmrc"     %% "emailaddress"                                  % "3.7.0"
  // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.jsoup"       %  "jsoup"                  % "1.15.4"
  // format: ON
  ).map(_ % Test)
}
