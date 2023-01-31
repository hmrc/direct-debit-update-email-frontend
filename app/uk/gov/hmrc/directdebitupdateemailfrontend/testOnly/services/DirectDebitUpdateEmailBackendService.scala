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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.services

import cats.syntax.eq._

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.journey.{SjRequest, SjResponse}
import ddUpdateEmail.models.{NextUrl, Origin}
import ddUpdateEmail.utils.Errors
import play.api.http.Status.CREATED
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.connectors.DirectDebitUpdateEmailBackendConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DirectDebitUpdateEmailBackendService @Inject() (
    connector: DirectDebitUpdateEmailBackendConnector
)(implicit ec: ExecutionContext) {

  def start(origin: Origin, sjRequest: SjRequest)(implicit hc: HeaderCarrier): Future[NextUrl] =
    connector.start(origin, sjRequest).map { response =>
      if (response.status === CREATED)
        response.json.validate[SjResponse.Success].fold(
          e => Errors.throwServerErrorException(s"Could not parse response body to start journey request. " +
            s"Body was ${response.body}, errors were ${e.toString}"),
          _.nextUrl
        )
      else
        Errors.throwServerErrorException(s"Got status ${response.status.toString} in response to start journey request. " +
          s"Response body was ${response.body}")

    }

}
