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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.EmailVerificationPasscodes
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationConnector @Inject() (servicesConfig: ServicesConfig, httpClient: HttpClientV2)(using
  ExecutionContext
) {

  private val getPasscodesUrl: String = servicesConfig.baseUrl("email-verification") + "/test-only/passcodes"

  def requestEmailVerification()(using HeaderCarrier): Future[EmailVerificationPasscodes] =
    httpClient
      .get(url"$getPasscodesUrl")
      .execute[EmailVerificationPasscodes]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode === NOT_FOUND => EmailVerificationPasscodes(List.empty)
      }

}
