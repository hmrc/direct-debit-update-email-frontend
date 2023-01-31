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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.connectors

import com.google.inject.Inject
import ddUpdateEmail.models.Origin
import ddUpdateEmail.models.journey.SjRequest
import play.api.Configuration
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class DirectDebitUpdateEmailBackendConnector @Inject() (
    httpClient: HttpClient,
    config:     Configuration
)(implicit ec: ExecutionContext) extends ServicesConfig(config) {

  private val baseUrl: String = baseUrl("direct-debit-update-email-backend")

  private val btaInternalAuthToken: String = config.get[String]("direct-debit-update-email-backend.start.internal-auth-token.bta")
  private val epayeInternalAuthToken: String = config.get[String]("direct-debit-update-email-backend.start.internal-auth-token.epaye")

  private val btaStartUrl: String = s"$baseUrl/direct-debit-update-email/bta/start"
  private val epayeStartUrl: String = s"$baseUrl/direct-debit-update-email/epaye/start"

  def start(origin: Origin, sjRequest: SjRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val (url, internalAuthToken) = origin match {
      case Origin.BTA          => btaStartUrl -> btaInternalAuthToken
      case Origin.EpayeService => epayeStartUrl -> epayeInternalAuthToken
    }

    val headers = Seq(HeaderNames.authorisation -> internalAuthToken)
    httpClient.POST[SjRequest, HttpResponse](url, sjRequest, headers)(
      implicitly[Writes[SjRequest]],
      implicitly[HttpReads[HttpResponse]],
      implicitly[HeaderCarrier].copy(authorization = None),
      ec
    )
  }

}
