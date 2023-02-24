/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.directdebitupdateemailfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.concurrent.duration.FiniteDuration

@Singleton
class AppConfig @Inject() (config: Configuration) {

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val authTimeoutSeconds: Int = config.get[FiniteDuration]("timeout-dialog.timeout").toSeconds.toInt
  val authTimeoutCountdownSeconds: Int = config.get[FiniteDuration]("timeout-dialog.countdown").toSeconds.toInt

  object BaseUrl {
    val platformHost: Option[String] = config.getOptional[String]("platform.frontend.host")
    val accessibilityStatementFrontend: String = config.get[String]("baseUrl.accessibility-statement-frontend-local")
    val ddUpdateEmailFrontend: String = platformHost.getOrElse(config.get[String]("baseUrl.direct-debit-update-email-frontend-local"))
    val gg: String = config.get[String]("baseUrl.gg")
    val signOutUrl: String = config.get[String]("baseUrl.sign-out")
    val contactFrontend: String = platformHost.getOrElse(config.get[String]("baseUrl.contact-frontend-local"))
    val betaFeedbackUrl: String = s"$contactFrontend/contact/beta-feedback?service=direct-debit-frontend"
  }

}
