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

import ddUpdateEmail.models.{EmailVerificationResult, StartEmailVerificationJourneyResult}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.DocumentUtils.DocumentOps
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.{ContentAssertions, ItSpec}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs.{AuthStub, DirectDebitUpdateEmailBackendStub}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.http.UpstreamErrorResponse

class EmailVerificationResultControllerSpec extends ItSpec {

  lazy val controller = app.injector.instanceOf[EmailVerificationResultController]

  s"GET ${routes.EmailVerificationResultController.emailConfirmed.url}" - {

    behave like authenticatedJourneyBehaviour(controller.emailConfirmed)

    "return an error if an email verification result has not been obtained yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.EmailVerificationJourneyStarted.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.emailConfirmed(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
      error.message should include("Cannot show email confirmed page before email verification result has been obtained.")
    }

    "return an error if the email verification result is locked" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson(emailVerificationResult = EmailVerificationResult.Locked)
      )

      val error = intercept[UpstreamErrorResponse](await(controller.emailConfirmed(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
      error.message should include("Cannot show email confirmed page when email verification result is 'Locked'")
    }

    "show the page if the email verification result is Verified" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson()
      )

      val result = controller.emailConfirmed(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))

      ContentAssertions.commonPageChecks(
        doc,
        "Email address verified",
        None,
        hasBackLink = false
      )

      val paragraphs = doc.selectList(".govuk-body")
      paragraphs.size shouldBe 3

      paragraphs(0).html() shouldBe s"We’ll use <strong>${TestData.selectedEmail.value.decryptedValue}</strong> to contact you about your Direct Debit."

      val continueButton = doc.select(".govuk-button")
      continueButton.attr("role") shouldBe "button"
      continueButton.attr("href") shouldBe TestData.sjRequest.returnUrl.value
      continueButton.text() shouldBe "Continue"

    }

  }

  s"GET ${routes.EmailVerificationResultController.tooManyPasscodeAttempts.url}" - {

    behave like authenticatedJourneyBehaviour(controller.tooManyPasscodeAttempts)

    "return an error if an email verification result has not been obtained yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.EmailVerificationJourneyStarted.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.tooManyPasscodeAttempts(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
      error.message should include("Cannot show tooManyPasscodeAttempts page before email verification result has been obtained.")
    }

    "return an error if the email verification result is Verified" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson()
      )

      val error = intercept[UpstreamErrorResponse](await(controller.tooManyPasscodeAttempts(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
      error.message should include("Cannot show tooManyPasscodeAttempts page when email verification result is 'Verified'")
    }

    "show the page if the email verification result is Locked" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson(emailVerificationResult = EmailVerificationResult.Locked)
      )

      val result = controller.tooManyPasscodeAttempts(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK
    }

  }

  s"GET ${routes.EmailVerificationResultController.tooManyPasscodeJourneysStarted.url}" - {

    behave like authenticatedJourneyBehaviour(controller.tooManyPasscodeJourneysStarted)

    "return an error if an email verification journey has not been started yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.tooManyPasscodeJourneysStarted(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
      error.message should include("Cannot show tooManyPasscodeJourneysStarted page before email verification journey has been started.")
    }

    List(
      StartEmailVerificationJourneyResult.Ok("/redirect"),
      StartEmailVerificationJourneyResult.AlreadyVerified,
      StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses,
      StartEmailVerificationJourneyResult.TooManyPasscodeAttempts
    ).foreach { startResult =>
        s"return an error if the start email verification journey result is ${startResult.getClass.getSimpleName}" in {
          AuthStub.authorise()
          DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
            TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = startResult)
          )

          val error = intercept[UpstreamErrorResponse](await(controller.tooManyPasscodeJourneysStarted(TestData.fakeRequestWithAuthorization)))
          error.statusCode shouldBe INTERNAL_SERVER_ERROR
          error.message should include("Cannot show tooManyPasscodeJourneysStarted when start verification journey result " +
            s"is ${startResult.getClass.getSimpleName}")
        }
      }

    "show the page if the start email verification journey result is TooManyPasscodeJourneysStarted" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(
          startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted
        )
      )

      val result = controller.tooManyPasscodeJourneysStarted(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK
    }

  }

  s"GET ${routes.EmailVerificationResultController.tooManyDifferentEmailAddresses.url}" - {

    behave like authenticatedJourneyBehaviour(controller.tooManyDifferentEmailAddresses)

    "return an error if an email verification journey has not been started yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.tooManyDifferentEmailAddresses(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
      error.message should include("Cannot show tooManyDifferentEmailAddresses page before email verification journey has been started.")
    }

    List(
      StartEmailVerificationJourneyResult.Ok("/redirect"),
      StartEmailVerificationJourneyResult.AlreadyVerified,
      StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted,
      StartEmailVerificationJourneyResult.TooManyPasscodeAttempts
    ).foreach { startResult =>
        s"return an error if the start email verification journey result is ${startResult.getClass.getSimpleName}" in {
          AuthStub.authorise()
          DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
            TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = startResult)
          )

          val error = intercept[UpstreamErrorResponse](await(controller.tooManyDifferentEmailAddresses(TestData.fakeRequestWithAuthorization)))
          error.statusCode shouldBe INTERNAL_SERVER_ERROR
          error.message should include("Cannot show tooManyDifferentEmailAddresses when start verification journey result " +
            s"is ${startResult.getClass.getSimpleName}")
        }
      }

    "show the page if the start email verification journey result is TooManyPasscodeJourneysStarted" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(
          startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses
        )
      )

      val result = controller.tooManyDifferentEmailAddresses(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK
    }

  }
}
