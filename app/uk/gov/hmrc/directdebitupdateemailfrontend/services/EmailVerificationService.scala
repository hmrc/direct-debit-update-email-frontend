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

package uk.gov.hmrc.directdebitupdateemailfrontend.services

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.Email
import paymentsEmailVerification.connectors.PaymentsEmailVerificationConnector
import paymentsEmailVerification.models.api.{GetEmailVerificationResultRequest, StartEmailVerificationJourneyRequest, StartEmailVerificationJourneyResponse}
import paymentsEmailVerification.models.{EmailVerificationResult, Email => PaymentsEmailVerificationEmail}
import play.api.mvc.Request
import uk.gov.hmrc.directdebitupdateemailfrontend.config.AppConfig
import uk.gov.hmrc.directdebitupdateemailfrontend.controllers.routes
import uk.gov.hmrc.directdebitupdateemailfrontend.messages.Messages
import uk.gov.hmrc.directdebitupdateemailfrontend.utils.RequestSupport
import uk.gov.hmrc.hmrcfrontend.config.ContactFrontendConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class EmailVerificationService @Inject() (
    appConfig:                  AppConfig,
    emailVerificationConnector: PaymentsEmailVerificationConnector,
    contactFrontendConfig:      ContactFrontendConfig,
    requestSupport:             RequestSupport
) {

  private val isLocal: Boolean = appConfig.BaseUrl.platformHost.isEmpty

  def startEmailVerificationJourney(email: Email)(implicit r: Request[_], hc: HeaderCarrier): Future[StartEmailVerificationJourneyResponse] = {
    val lang = requestSupport.language(r)

    val startRequest = StartEmailVerificationJourneyRequest(
      RequestEmailVerification.continueUrl,
      RequestEmailVerification.origin,
      RequestEmailVerification.deskproServiceName,
      RequestEmailVerification.accessibilityStatementUrl,
      Messages.`Check or change your Direct Debit email address`.show(lang),
      RequestEmailVerification.emailEntryUrl,
      RequestEmailVerification.emailEntryUrl,
      PaymentsEmailVerificationEmail(email.value.decryptedValue),
      lang.code
    )

    emailVerificationConnector.startEmailVerification(startRequest)
  }

  def getVerificationResult(email: Email)(implicit hc: HeaderCarrier): Future[EmailVerificationResult] =
    emailVerificationConnector.getEmailVerificationResult(GetEmailVerificationResultRequest(PaymentsEmailVerificationEmail(email.value.decryptedValue)))

  private object RequestEmailVerification {
    private def ddUpdateEmailFrontendUrl(s: String): String = if (isLocal) s"${appConfig.BaseUrl.ddUpdateEmailFrontend}$s" else s
    val continueUrl: String = ddUpdateEmailFrontendUrl(routes.CallbackController.callback.url)
    val origin: String = "direct-debit-update-email-frontend"
    val deskproServiceName: String = contactFrontendConfig.serviceId.getOrElse(sys.error("Could not find contact frontend serviceId"))
    val accessibilityStatementUrl: String = {
      val u = "/accessibility-statement/direct-debit-verify-email"
      if (isLocal) s"${appConfig.BaseUrl.accessibilityStatementFrontend}$u" else u
    }
    val emailEntryUrl = ddUpdateEmailFrontendUrl(routes.EmailController.selectEmail.url)
  }

}
