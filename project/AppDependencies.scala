import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.5.0"

  val compile = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-frontend-play-30"                    % bootstrapVersion,
    "uk.gov.hmrc"     %% "play-frontend-hmrc-play-30"                    % "12.26.0",
    "uk.gov.hmrc"     %% "play-conditional-form-mapping-play-30"         % "3.4.0",
    "com.beachape"    %% "enumeratum-play"                               % "1.9.2",
    "org.typelevel"   %% "cats-core"                                     % "2.13.0",
    "uk.gov.hmrc"     %% "direct-debit-update-email-backend-cor-journey" % "1.5.0",
    "uk.gov.hmrc"     %% "payments-email-verification-cor-play-30"       % "4.7.0"
  // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"     %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"       %  "jsoup"                  % "1.22.1"
  // format: ON
  ).map(_ % Test)
}
