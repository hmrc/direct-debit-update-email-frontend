import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.2.0"

  val compile = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-28"                    % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc"                            % "7.29.0-play-28",
    "uk.gov.hmrc"     %% "play-conditional-form-mapping"                 % "1.13.0-play-28",
    "com.beachape"    %% "enumeratum-play"                               % "1.7.3",
    "org.typelevel"   %% "cats-core"                                     % "2.10.0",
    "uk.gov.hmrc"     %% "direct-debit-update-email-backend-cor-journey" % "0.14.0",
    "uk.gov.hmrc"     %% "payments-email-verification-cor"               % "1.1.0",
    "uk.gov.hmrc"     %% "emailaddress"                                  % "3.8.0"
  // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.jsoup"       %  "jsoup"                  % "1.17.1",
  // format: ON
  ).map(_ % Test)
}
