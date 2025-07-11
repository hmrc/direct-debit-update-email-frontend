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

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.journey.{SjRequest, SjResponse}
import ddUpdateEmail.models.{BackUrl, NextUrl, ReturnUrl, TaxRegime}
import play.api.mvc.{Request, Session}
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.controllers.routes
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.forms.StartJourneyForm
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.testuser.TestUser
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.{DirectDebitRecord, TaxId}
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.utils.RandomDataGenerator
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StartService @Inject() (
  authLoginApiService:                  AuthLoginApiService,
  directDebitBackendService:            DirectDebitBackendService,
  directDebitUpdateEmailBackendService: DirectDebitUpdateEmailBackendService
)(using ExecutionContext) {

  def start(
    formData: StartJourneyForm
  )(using request: Request[?]): Future[Either[SjResponse.Error, (Session, NextUrl)]] = {
    lazy val ddiNumber = RandomDataGenerator.nextDdiNumber()

    lazy val directDebitRecord = {
      val taxId = formData.taxRegime match {
        case TaxRegime.Paye => TaxId("empref", RandomDataGenerator.nextEmpref())
        case TaxRegime.Cds  => sys.error("CDS unsupported")
        case TaxRegime.Ppt  => TaxId("zppt", RandomDataGenerator.nextZpptRef())
        case TaxRegime.Zsdl => TaxId("zsdl", RandomDataGenerator.nextZsdlRef())
        case TaxRegime.VatC => TaxId("vrn", RandomDataGenerator.nextVrn())
      }

      DirectDebitRecord(
        formData.taxRegime,
        Some(taxId),
        formData.email,
        List(ddiNumber),
        formData.isBounced
      )
    }

    lazy val sjRequest = SjRequest(
      ddiNumber,
      BackUrl(routes.StartJourneyController.dummyBack.url),
      ReturnUrl(routes.StartJourneyController.dummyReturn.url)
    )

    for {
      session <- authLoginApiService.logIn(TestUser.makeTestUser(formData))
      hc       = HeaderCarrierConverter.fromRequestAndSession(
                   request.withHeaders(request.headers.remove(HeaderNames.xSessionId)),
                   session
                 )
      _       <- directDebitBackendService.insertRecord(directDebitRecord)(hc)
      result  <- directDebitUpdateEmailBackendService.start(formData.origin, sjRequest)(hc)
    } yield result.map(session -> _)
  }

}
