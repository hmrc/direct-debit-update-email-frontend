import sbt.Setting
import scoverage.ScoverageKeys

object ScoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    ".*Routes.*",
    "uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.*",
    "testOnlyDoNotUseInAppConf.*"
  )

  val scoverageSettings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 75, // to be bumped up when first page is built
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
