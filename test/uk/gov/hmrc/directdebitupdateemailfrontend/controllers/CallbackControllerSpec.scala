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

package uk.gov.hmrc.directdebitupdateemailfrontend.controllers

import ddUpdateEmail.models.EmailVerificationResult
import ddUpdateEmail.models.TaxId.{EmpRef, Vrn, Zppt, Zsdl}
import paymentsEmailVerification.models.{EmailVerificationResult => PaymentsEmailVerificationResult}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.ItSpec
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs._
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.http.UpstreamErrorResponse

class CallbackControllerSpec extends ItSpec {

  lazy val controller = app.injector.instanceOf[CallbackController]

  s"GET ${routes.CallbackController.callback.url}" - {

    behave like authenticatedJourneyBehaviour(controller.callback)

    "must return an error if an email verification journey hasn't been started yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.callback(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
    }

    List(
      ("BTA", "paye", "empref", EmpRef("1234567")),
      ("BTA", "vatc", "vrn", Vrn("123456")),
      ("EpayeService", "ppt", "zppt", Zppt("12345")),
      ("EpayeService", "zsdl", "zsdl", Zsdl("1234"))
    ).foreach {
        case (origin, taxRegimeString, taxIdType, taxId) =>
          "redirect to the email verified page if the email has been verified for " +
            s"origin=$origin, taxRegime=$taxRegimeString and taxIdType=$taxIdType" in {
              AuthStub.authorise()
              DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
                TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(origin    = origin, taxRegime = taxRegimeString, taxId = Some(taxId))
              )
              EmailVerificationStub.getVerificationStatus(PaymentsEmailVerificationResult.Verified)
              DirectDebitBackendStub.updateEmailAndBouncedFlag(TestData.ddiNumber)
              DirectDebitUpdateEmailBackendStub.updateEmailVerificationResult(TestData.journeyId, TestData.Journeys.ObtainedEmailVerificationResult.journeyJson())

              val result = controller.callback(TestData.fakeRequestWithAuthorization)
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.emailConfirmed.url)

              EmailVerificationStub.verifyGetEmailVerificationResult(TestData.selectedEmail)
              AuditStub.verifyEventAudited(
                "EmailVerificationResult",
                Json.parse(
                  s"""{
                 |  "origin": "$origin",
                 |  "taxType": "$taxRegimeString",
                 |  "taxId": "${taxId.value}",
                 |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
                 |  "emailSource": "New",
                 |  "result": "Verified",
                 |  "authProviderId": "${TestData.ggCredId.value}"
                 |}""".stripMargin
                ).as[JsObject]
              )
              DirectDebitBackendStub.verifyUpdateEmailAndBouncedFlag(
                TestData.ddiNumber,
                TestData.selectedEmail,
                isBounced = false
              )
              DirectDebitUpdateEmailBackendStub.verifyUpdateEmailVerificationResult(TestData.journeyId, EmailVerificationResult.Verified)
            }
      }

    "return an error if the email has been verified but there is a problem updating direct-debit-backend" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(taxId         = None, selectedEmail = TestData.bouncedEmail)
      )
      EmailVerificationStub.getVerificationStatus(PaymentsEmailVerificationResult.Verified)
      DirectDebitBackendStub.updateEmailAndBouncedFlag(TestData.ddiNumber, INTERNAL_SERVER_ERROR)

      val error = intercept[UpstreamErrorResponse](
        await(controller.callback(TestData.fakeRequestWithAuthorization))
      )
      error.statusCode shouldBe INTERNAL_SERVER_ERROR

      EmailVerificationStub.verifyGetEmailVerificationResult(TestData.bouncedEmail)
      AuditStub.verifyEventAudited(
        "EmailVerificationResult",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.bouncedEmail.value.decryptedValue}",
             |  "emailSource": "Original",
             |  "result": "Verified",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitBackendStub.verifyUpdateEmailAndBouncedFlag(
        TestData.ddiNumber,
        TestData.bouncedEmail,
        isBounced = false
      )
    }

    "redirect to the too many passcode attempts if the user has attempted too many passcodes" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.EmailVerificationJourneyStarted.journeyJson())
      EmailVerificationStub.getVerificationStatus(PaymentsEmailVerificationResult.Locked)
      DirectDebitUpdateEmailBackendStub.updateEmailVerificationResult(
        TestData.journeyId,
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson(emailVerificationResult = EmailVerificationResult.Locked)
      )

      val result = controller.callback(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.tooManyPasscodeAttempts.url)

      EmailVerificationStub.verifyGetEmailVerificationResult(TestData.selectedEmail)
      AuditStub.verifyEventAudited(
        "EmailVerificationResult",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "Locked",
             |  "failureReason": "TooManyPasscodeAttempts",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateEmailVerificationResult(TestData.journeyId, EmailVerificationResult.Locked)
    }

  }

}
