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

import paymentsEmailVerification.models.EmailVerificationResult
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.ItSpec
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs.{AuthStub, DirectDebitUpdateEmailBackendStub, EmailVerificationStub}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.http.UpstreamErrorResponse

class CallbackControllerSpec extends ItSpec {

  lazy val controller = app.injector.instanceOf[CallbackController]

  s"GET ${routes.CallbackController.callback.url}" - {

    behave like authenticatedJourneyBehaviour(controller.callback)

    "must return an error if an email address hasn't been selected yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.Started.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.callback(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to the email verified page if the email has been verified" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
      EmailVerificationStub.getVerificationStatus(EmailVerificationResult.Verified)

      val result = controller.callback(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.emailConfirmed.url)

      EmailVerificationStub.verifyGetEmailVerificationResult(TestData.selectedEmail)
    }

    "redirect to the too many passcode attempts if the user has attempted too many passcodes" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
      EmailVerificationStub.getVerificationStatus(EmailVerificationResult.Locked)

      val result = controller.callback(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.tooManyPasscodeAttempts.url)

      EmailVerificationStub.verifyGetEmailVerificationResult(TestData.selectedEmail)
    }

  }

}
