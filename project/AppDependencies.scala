import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.2.0"

  val compile = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-30"                    % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc-play-30"                    % "8.2.0",
    "uk.gov.hmrc"     %% "play-conditional-form-mapping-play-30"         % "2.0.0",
    "com.beachape"    %% "enumeratum-play"                               % "1.8.0",
    "org.typelevel"   %% "cats-core"                                     % "2.10.0",
    "uk.gov.hmrc"     %% "direct-debit-update-email-backend-cor-journey" % "0.15.0",
    "uk.gov.hmrc"     %% "payments-email-verification-cor-play-30"       % "2.0.0",
    "uk.gov.hmrc"     %% "emailaddress-play-30"                          % "4.0.0"
  // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"       %  "jsoup"                  % "1.17.1",
  // format: ON
  ).map(_ % Test)
}
