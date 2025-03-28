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

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.crypto.CryptoFormat
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.DirectDebitRecord
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DirectDebitBackendConnector @Inject() (
  httpClient: HttpClientV2,
  config:     Configuration
)(using ExecutionContext)
    extends ServicesConfig(config) {

  private given CryptoFormat = CryptoFormat.NoOpCryptoFormat

  private val baseUrl: String = baseUrl("direct-debit-backend")

  private val insertRecordUrl: String = s"$baseUrl/direct-debit-backend/test-only/bounced-email/status"

  def insertRecord(directDebitRecord: DirectDebitRecord)(using HeaderCarrier): Future[HttpResponse] =
    httpClient.post(url"$insertRecordUrl").withBody(Json.toJson(directDebitRecord)).execute[HttpResponse]

}
