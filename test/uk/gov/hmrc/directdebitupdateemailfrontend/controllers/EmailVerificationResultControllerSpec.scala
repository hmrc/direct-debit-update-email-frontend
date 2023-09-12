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
import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.DocumentUtils.DocumentOps
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.FakeRequestUtils.FakeRequestOps
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.{ContentAssertions, ItSpec}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs.{AuthStub, DirectDebitUpdateEmailBackendStub, EmailVerificationStub}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.LocalDateTime

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

      val request = TestData.fakeRequestWithAuthorization
      val result = controller.emailConfirmed(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))

      ContentAssertions.commonPageChecks(
        doc,
        "Email address verified",
        None,
        hasBackLink = false
      )(request)

      val paragraphs = doc.selectList(".govuk-body")
      paragraphs.size shouldBe 3

      paragraphs(0).html() shouldBe s"We’ll use <strong>${TestData.selectedEmail.value.decryptedValue}</strong> to contact you about your Direct Debit."
      paragraphs(1).text shouldBe "Your email address has not been changed in other government services."

      val continueButton = doc.select(".govuk-button")
      continueButton.attr("role") shouldBe "button"
      continueButton.attr("href") shouldBe TestData.sjRequest.returnUrl.value
      continueButton.text() shouldBe "Continue"
    }

    "show the page in Welsh" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson()
      )

      val request = TestData.fakeRequestWithAuthorization.withLang(Language.Welsh)
      val result = controller.emailConfirmed(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))

      ContentAssertions.commonPageChecks(
        doc,
        "Cyfeiriad e-bost wedi’i ddilysu",
        None,
        hasBackLink = false,
        language    = Language.Welsh
      )(request)

      val paragraphs = doc.selectList(".govuk-body")
      paragraphs.size shouldBe 3

      paragraphs(0).html() shouldBe s"Byddwn yn defnyddio <strong>${TestData.selectedEmail.value.decryptedValue}</strong> i gysylltu â chi ynghylch eich Debyd Uniongyrchol."
      paragraphs(1).text shouldBe "Nid yw’ch e-bost wedi cael ei newid ar gyfer gwasanaethau eraill y llywodraeth."

      val continueButton = doc.select(".govuk-button")
      continueButton.attr("role") shouldBe "button"
      continueButton.attr("href") shouldBe TestData.sjRequest.returnUrl.value
      continueButton.text() shouldBe "Yn eich blaen"
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

      val request = TestData.fakeRequestWithAuthorization
      val result = controller.tooManyPasscodeAttempts(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))
      ContentAssertions.commonPageChecks(
        doc,
        "Email verification code entered too many times",
        None,
        hasBackLink = false
      )(request)

      val paragraphs = doc.selectList("p.govuk-body")
      paragraphs.size shouldBe 2

      paragraphs(0).text() shouldBe "You have entered an email verification code too many times."
      paragraphs(1).text() shouldBe "You can go back to enter a new email address."

      val link = doc.select("p > a.govuk-link")
      link.text() shouldBe "go back to enter a new email address"
      link.attr("href") shouldBe routes.EmailController.selectEmail.url
    }

    "display the page in Welsh" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson(emailVerificationResult = EmailVerificationResult.Locked)
      )

      val request = TestData.fakeRequestWithAuthorization.withLang(Language.Welsh)
      val result = controller.tooManyPasscodeAttempts(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))
      ContentAssertions.commonPageChecks(
        doc,
        "Cod dilysu e-bost wedi’i nodi gormod o weithiau",
        None,
        hasBackLink = false,
        language    = Language.Welsh
      )(request)

      val paragraphs = doc.selectList("p.govuk-body")
      paragraphs.size shouldBe 2

      paragraphs(0).text() shouldBe "Rydych chi wedi nodi cod dilysu e-bost gormod o weithiau."
      paragraphs(1).text() shouldBe "Gallwch fynd yn ôl i nodi cyfeiriad e-bost newydd."

      val link = doc.select("p > a.govuk-link")
      link.text() shouldBe "fynd yn ôl i nodi cyfeiriad e-bost newydd"
      link.attr("href") shouldBe routes.EmailController.selectEmail.url
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

      val request = TestData.fakeRequestWithAuthorization
      val result = controller.tooManyPasscodeJourneysStarted(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))
      ContentAssertions.commonPageChecks(
        doc,
        "You have tried to verify an email address too many times",
        None,
        hasBackLink = false
      )(request)

      val paragraphs = doc.selectList("p.govuk-body")
      paragraphs.size shouldBe 2

      paragraphs(0).text() shouldBe s"You have tried to verify ${TestData.selectedEmail.value.decryptedValue} too many times."
      paragraphs(1).text() shouldBe "You will need to verify a different email address."

      val link = doc.select("p > a.govuk-link")
      link.text() shouldBe "verify a different email address"
      link.attr("href") shouldBe routes.EmailController.selectEmail.url
    }

    "display the page in Welsh" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(
          startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted
        )
      )

      val request = TestData.fakeRequestWithAuthorization.withLang(Language.Welsh)
      val result = controller.tooManyPasscodeJourneysStarted(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))
      ContentAssertions.commonPageChecks(
        doc,
        "Rydych wedi ceisio dilysu cyfeiriad e-bost gormod o weithiau",
        None,
        hasBackLink = false,
        language    = Language.Welsh
      )(request)

      val paragraphs = doc.selectList("p.govuk-body")
      paragraphs.size shouldBe 2

      paragraphs(0).text() shouldBe s"Rydych wedi ceisio dilysu ${TestData.selectedEmail.value.decryptedValue} gormod o weithiau."
      paragraphs(1).text() shouldBe "Bydd angen i chi ddilysu cyfeiriad e-bost gwahanol."

      val link = doc.select("p > a.govuk-link")
      link.text() shouldBe "ddilysu cyfeiriad e-bost gwahanol"
      link.attr("href") shouldBe routes.EmailController.selectEmail.url
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

    "should return an error if the lockout expiry time cannot be determined" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(
          startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses
        )
      )
      EmailVerificationStub.getLockoutCreatedAt(None)

      val error = intercept[UpstreamErrorResponse](
        await(controller.tooManyDifferentEmailAddresses(TestData.fakeRequestWithAuthorization))
      )
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
    }

    "show the page if the start email verification journey result is TooManyPasscodeJourneysStarted" in {
      val dateTime = LocalDateTime.of(2023, 3, 12, 11, 34, 43)
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(
          startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses
        )
      )
      EmailVerificationStub.getLockoutCreatedAt(Some(dateTime))

      val request = TestData.fakeRequestWithAuthorization
      val result = controller.tooManyDifferentEmailAddresses(request)
      status(result) shouldBe OK

      val doc = Jsoup.parse(contentAsString(result))
      ContentAssertions.commonPageChecks(
        doc,
        "You have tried to verify too many email addresses",
        None,
        hasBackLink = false
      )(request)

      doc.select("p.govuk-body").first.text() shouldBe "You have been locked out because you have tried to verify too many " +
        "email addresses. Please try again on 13 March 2023 at 11:34am."

      val button = doc.select(".govuk-button")
      button.text() shouldBe "Return to tax account"
      button.attr("href") shouldBe TestData.sjRequest.backUrl.value

    }

    "display the page in Welsh" in {
      List(
        1 -> "Ionawr",
        2 -> "Chwefror",
        3 -> "Mawrth",
        4 -> "Ebrill",
        5 -> "Mai",
        6 -> "Mehefin",
        7 -> "Gorffennaf",
        8 -> "Awst",
        9 -> "Medi",
        10 -> "Hydref",
        11 -> "Tachwedd",
        12 -> "Rhagfyr"
      ).foreach {
          case (monthInt, expectedWelshMonth) =>
            withClue(s"For month ${monthInt.toString}: ") {
              val dateTime = LocalDateTime.of(2023, monthInt, 12, 11, 34, 43)
              AuthStub.authorise()
              DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
                TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(
                  startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses
                )
              )
              EmailVerificationStub.getLockoutCreatedAt(Some(dateTime))

              val request = TestData.fakeRequestWithAuthorization.withLang(Language.Welsh)
              val result = controller.tooManyDifferentEmailAddresses(request)
              status(result) shouldBe OK

              val doc = Jsoup.parse(contentAsString(result))
              ContentAssertions.commonPageChecks(
                doc,
                "Rydych wedi ceisio dilysu gormod o gyfeiriadau e-bost",
                None,
                hasBackLink = false,
                language    = Language.Welsh
              )(request)

              doc.select("p.govuk-body").first.text() shouldBe "Rydych chi wedi cael eich cloi allan oherwydd eich bod wedi ceisio dilysu gormod o gyfeiriadau e-bost. " +
                s"Rhowch gynnig arall arni ar 13 $expectedWelshMonth 2023 am 11:34am."

              val button = doc.select(".govuk-button")
              button.text() shouldBe "Yn ôl i’r cyfrif treth"
              button.attr("href") shouldBe TestData.sjRequest.backUrl.value
            }
        }
    }

  }
}
