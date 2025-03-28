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

package uk.gov.hmrc.directdebitupdateemailfrontend.connectors

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.crypto.CryptoFormat
import ddUpdateEmail.models.{DDINumber, Email}
import play.api.Configuration
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DirectDebitBackendConnector @Inject() (
  httpClient: HttpClientV2,
  config:     Configuration
)(using ExecutionContext)
    extends ServicesConfig(config) {

  import DirectDebitBackendConnector._

  private val baseUrl: String = baseUrl("direct-debit-backend")

  private def updateEmailAndBouncedFlagUrl(ddiNumber: DDINumber): String =
    s"$baseUrl/direct-debit-backend/bounced-email/status/${ddiNumber.value}"

  def updateEmailAndBouncedFlag(ddiNumber: DDINumber, email: Email, isBounced: Boolean)(using
    HeaderCarrier
  ): Future[HttpResponse] =
    httpClient
      .post(url"${updateEmailAndBouncedFlagUrl(ddiNumber)}")
      .withBody(Json.toJson(BouncedEmailUpdateRequest(email, isBounced)))
      .execute[HttpResponse]

}

object DirectDebitBackendConnector {

  private final case class BouncedEmailUpdateRequest(email: Email, isBounced: Boolean)

  private given OWrites[BouncedEmailUpdateRequest] = {
    given CryptoFormat = CryptoFormat.NoOpCryptoFormat
    Json.writes
  }

}
