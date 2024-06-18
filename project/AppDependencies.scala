import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.0.0"

  val compile = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-30"                    % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc-play-30"                    % "10.1.0",
    "uk.gov.hmrc"     %% "play-conditional-form-mapping-play-30"         % "2.0.0",
    "com.beachape"    %% "enumeratum-play"                               % "1.8.0",
    "org.typelevel"   %% "cats-core"                                     % "2.12.0",
    "uk.gov.hmrc"     %% "direct-debit-update-email-backend-cor-journey" % "0.18.0",
    "uk.gov.hmrc"     %% "payments-email-verification-cor-play-30"       % "3.0.0"
  // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"       %  "jsoup"                  % "1.17.2"
  // format: ON
  ).map(_ % Test)
}
