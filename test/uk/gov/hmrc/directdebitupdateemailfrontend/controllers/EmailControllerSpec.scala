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

import ddUpdateEmail.models.TaxId.{EmpRef, Vrn, Zppt, Zsdl}
import ddUpdateEmail.models.{EmailVerificationResult, StartEmailVerificationJourneyResult}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import paymentsEmailVerification.models.EmailVerificationState.{AlreadyVerified, TooManyDifferentEmailAddresses, TooManyPasscodeAttempts, TooManyPasscodeJourneysStarted}
import paymentsEmailVerification.models.api.StartEmailVerificationJourneyResponse
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Cookie
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.{ContentAssertions, ItSpec}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs.{AuditStub, AuthStub, DirectDebitBackendStub, DirectDebitUpdateEmailBackendStub, EmailVerificationStub}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.DocumentUtils._
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.FakeRequestUtils.FakeRequestOps
import uk.gov.hmrc.http.UpstreamErrorResponse

class EmailControllerSpec extends ItSpec {

  lazy val controller = app.injector.instanceOf[EmailController]

  s"GET ${routes.EmailController.selectEmail.url}" - {

      def checkPageContents(doc: Document): Unit = {
        ContentAssertions.commonPageChecks(
          doc,
          "Check or change your email address",
          Some(routes.EmailController.selectEmailSubmit.url),
          backLinkOverrideUrl = Some(TestData.sjRequest.backUrl.value)
        )

        val paragraphs = doc.selectList("p.govuk-body")
        paragraphs.size shouldBe 2

        paragraphs(0).text shouldBe "We cannot contact you about your Employers’ PAYE Direct Debit using bounced@email.com."
        paragraphs(1).text shouldBe "The reason for this could be:"

        val listItems = doc.selectList(".govuk-list > li")
        listItems.size shouldBe 3
        listItems(0).text shouldBe "Your email inbox is full"
        listItems(1).text shouldBe "Your email address is not valid or it is spelt incorrectly"
        listItems(2).text shouldBe "Emails from HMRC have been marked as spam"

        doc.select(".govuk-fieldset__legend").text() shouldBe "Which email address do you want to use?"

        val radios = doc.selectList(".govuk-radios__item")
        radios.size shouldBe 2

        radios(0).text() shouldBe "Use a different email address"
        radios(1).text() shouldBe "Test bounced@email.com with a verification email"
        ()
      }

    behave like (authenticatedJourneyBehaviour(controller.selectEmail))

    "must display the page if a journey can be found" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.Started.journeyJson())

      val result = controller.selectEmail(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK
      val doc = Jsoup.parse(contentAsString(result))

      checkPageContents(doc)

      val radios = doc.selectList(".govuk-radios__item")
      radios.size shouldBe 2

      radios(0).select(".govuk-radios__input").hasAttr("checked") shouldBe false
      radios(1).select(".govuk-radios__input").hasAttr("checked") shouldBe false

      DirectDebitUpdateEmailBackendStub.verifyFindByLatestSessionId()
    }

    "must display the page if a journey can be found where a different email address had been chosen" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())

      val result = controller.selectEmail(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK
      val doc = Jsoup.parse(contentAsString(result))

      checkPageContents(doc)

      val radios = doc.selectList(".govuk-radios__item")
      radios.size shouldBe 2

      radios(0).select(".govuk-radios__input").hasAttr("checked") shouldBe true
      radios(1).select(".govuk-radios__input").hasAttr("checked") shouldBe false

      val newEmailInput = doc.select(".govuk-radios__conditional > .govuk-form-group > .govuk-input ")
      newEmailInput.`val` shouldBe TestData.selectedEmail.value.decryptedValue

      DirectDebitUpdateEmailBackendStub.verifyFindByLatestSessionId()
    }

    "must display the page if a journey can be found where the bounced email address had been chosen" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.SelectedEmail.journeyJson(selectedEmail = TestData.bouncedEmail)
      )

      val result = controller.selectEmail(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe OK
      val doc = Jsoup.parse(contentAsString(result))

      checkPageContents(doc)

      val radios = doc.selectList(".govuk-radios__item")
      radios.size shouldBe 2

      radios(0).select(".govuk-radios__input").hasAttr("checked") shouldBe false
      radios(1).select(".govuk-radios__input").hasAttr("checked") shouldBe true

      DirectDebitUpdateEmailBackendStub.verifyFindByLatestSessionId()
    }

    "must display the page in welsh" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.SelectedEmail.journeyJson(selectedEmail = TestData.bouncedEmail)
      )

      val result = controller.selectEmail(TestData.fakeRequestWithAuthorization.withLang(Language.Welsh))
      status(result) shouldBe OK
      val doc = Jsoup.parse(contentAsString(result))

      ContentAssertions.commonPageChecks(
        doc,
        "Gwirio neu newid eich cyfeiriad e-bost",
        Some(routes.EmailController.selectEmailSubmit.url),
        backLinkOverrideUrl = Some(TestData.sjRequest.backUrl.value),
        language            = Language.Welsh
      )

      val paragraphs = doc.selectList("p.govuk-body")
      paragraphs.size shouldBe 2

      paragraphs(0).text shouldBe "Ni allwn gysylltu â chi am eich Debyd Uniongyrchol ar gyfer TWE y Cyflogwr gan ddefnyddio bounced@email.com."
      paragraphs(1).text shouldBe "Gallai’r rheswm am hyn fod y naill o’r canlynol:"

      val listItems = doc.selectList(".govuk-list > li")
      listItems.size shouldBe 3
      listItems(0).text shouldBe "Mae mewnflwch eich e-bost yn llawn"
      listItems(1).text shouldBe "Mae cyfeiriad eich e-bost yn annilys neu heb gael ei sillafu’n gywir"
      listItems(2).text shouldBe "Mae e-byst gan CThEF wedi’u nodi fel sbam"

      doc.select(".govuk-fieldset__legend").text() shouldBe "Pa gyfeiriad e-bost ydych chi am ei ddefnyddio?"

      val radios = doc.selectList(".govuk-radios__item")
      radios.size shouldBe 2

      radios(0).text() shouldBe "Defnyddio cyfeiriad e-bost gwahanol"
      radios(1).text() shouldBe "Profi bounced@email.com gydag e-bost dilysu"
    }

  }

  s"POST ${routes.EmailController.selectEmailSubmit.url}" - {

    behave like (authenticatedJourneyBehaviour(controller.selectEmailSubmit))

    "return a form error when" - {

        def test(formData: (String, String)*)(expectedErrorMessageEnglish: String, expectedErrorMessageWelsh: String, expectedErrorTarget: String): Unit = {
          List(
            Language.English,
            Language.Welsh
          ).foreach{ lang =>
              withClue(s"For language ${lang.entryName}: "){
                AuthStub.authorise()
                DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.Started.journeyJson())

                val request =
                  TestData.fakeRequestWithAuthorization
                    .withMethod("POST")
                    .withFormUrlEncodedBody(formData: _*)
                    .withLang(lang)
                val result = controller.selectEmailSubmit(request)

                status(result) shouldBe BAD_REQUEST
                val doc = Jsoup.parse(contentAsString(result))

                val expectedH1 = lang match {
                  case Language.English => "Check or change your email address"
                  case Language.Welsh   => "Gwirio neu newid eich cyfeiriad e-bost"
                }

                ContentAssertions.commonPageChecks(
                  doc,
                  expectedH1,
                  Some(routes.EmailController.selectEmailSubmit.url),
                  backLinkOverrideUrl = Some(TestData.sjRequest.backUrl.value),
                  hasFormError        = true,
                  language            = lang
                )

                val errorSummary = doc.select(".govuk-error-summary")
                val errorLink = errorSummary.select("a")
                errorLink.text() shouldBe (lang match {
                  case Language.English => expectedErrorMessageEnglish
                  case Language.Welsh   => expectedErrorMessageWelsh
                })
                errorLink.attr("href") shouldBe expectedErrorTarget
                ()
              }
            }
        }

      "nothing is submitted" in {
        test()(
          "Select which email address you want to use",
          "Dewiswch pa gyfeiriad e-bost rydych chi am ei ddefnyddio",
          "#selectAnEmailToUseRadio"
        )
      }

      "the user select to use a new email address but" - {

        "the email is empty" in {
          test(
            "selectAnEmailToUseRadio" -> "new",
            "newEmailInput" -> ""
          )(
              "Enter your email address in the correct format, like name@example.com",
              "Nodwch eich cyfeiriad e-bost yn y fformat cywir, megis enw@enghraifft.com",
              "#newEmailInput"
            )
        }

        "the email is longer than 256 characters" in {
          test(
            "selectAnEmailToUseRadio" -> "new",
            "newEmailInput" -> ("a" * 257)
          )(
              "Enter an email address with 256 characters or less",
              "Nodwch gyfeiriad e-bost gan ddefnyddio 256 o gymeriadau neu lai",
              "#newEmailInput"
            )
        }

        "the email address is not in the correct format" in {
          test(
            "selectAnEmailToUseRadio" -> "new",
            "newEmailInput" -> "invalidEmail"
          )(
              "Enter your email address in the correct format, like name@example.com",
              "Nodwch eich cyfeiriad e-bost yn y fformat cywir, megis enw@enghraifft.com",
              "#newEmailInput"
            )
        }
      }

    }

    "redirect to request-verification when" - {

      "a new email address is chosen" in {
        AuthStub.authorise()
        DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.Started.journeyJson())
        DirectDebitUpdateEmailBackendStub.updateSelectedEmail(TestData.journeyId, TestData.Journeys.SelectedEmail.journeyJson())

        val request = TestData.fakeRequestWithAuthorization.withMethod("POST").withFormUrlEncodedBody(
          "selectAnEmailToUseRadio" -> "new",
          "newEmailInput" -> TestData.selectedEmail.value.decryptedValue
        )
        val result = controller.selectEmailSubmit(request)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.EmailController.requestVerification.url)

        DirectDebitUpdateEmailBackendStub.verifyUpdateSelectedEmail(TestData.journeyId, TestData.selectedEmail)
      }

      "the bounced email address is chosen" in {
        AuthStub.authorise()
        DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.Started.journeyJson())
        DirectDebitUpdateEmailBackendStub.updateSelectedEmail(
          TestData.journeyId,
          TestData.Journeys.SelectedEmail.journeyJson(selectedEmail = TestData.bouncedEmail)
        )

        val request = TestData.fakeRequestWithAuthorization.withMethod("POST").withFormUrlEncodedBody(
          "selectAnEmailToUseRadio" -> TestData.bouncedEmail.value.decryptedValue,
          "newEmailInput" -> ""
        )
        val result = controller.selectEmailSubmit(request)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.EmailController.requestVerification.url)

        DirectDebitUpdateEmailBackendStub.verifyUpdateSelectedEmail(TestData.journeyId, TestData.bouncedEmail)
      }

    }

  }

  s"GET ${routes.EmailController.requestVerification.url}" - {

    behave like authenticatedJourneyBehaviour(controller.requestVerification)

    "must return an error if an email address hasn't been selected yet" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.Started.journeyJson())

      val error = intercept[UpstreamErrorResponse](await(controller.requestVerification(TestData.fakeRequestWithAuthorization)))
      error.statusCode shouldBe INTERNAL_SERVER_ERROR
    }

    List(
      ("BTA", "paye", "empref", EmpRef("1234567")),
      ("BTA", "vatc", "vrn", Vrn("123456")),
      ("EpayeService", "ppt", "zppt", Zppt("12345")),
      ("EpayeService", "zsdl", "zsdl", Zsdl("1234"))
    ).foreach {
        case (origin, taxRegimeString, taxIdType, taxId) =>

          s"must redirect to the given redirectUrl if the verification journey has successfully started for " +
            s"origin=$origin, taxRegime=$taxRegimeString and taxIdType=$taxIdType " in {
              AuthStub.authorise()
              DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
                TestData.Journeys.SelectedEmail.journeyJson(origin    = origin, taxRegime = taxRegimeString, taxId = Some(taxId))
              )
              EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Success(TestData.emailVerificationRedirectUrl))
              AuditStub.audit()
              DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
                TestData.journeyId,
                TestData.Journeys.EmailVerificationJourneyStarted.journeyJson()
              )

              val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(TestData.emailVerificationRedirectUrl)

              EmailVerificationStub.verifyRequestEmailVerification(
                TestData.selectedEmail,
                "http://localhost:12346/accessibility-statement/direct-debit-verify-email",
                "Check or change your Direct Debit email address",
                "en",
                "http://localhost:10801"
              )
              AuditStub.verifyEventAudited(
                "EmailVerificationRequested",
                Json.parse(
                  s"""{
                 |  "origin": "$origin",
                 |  "taxType": "$taxRegimeString",
                 |  "taxId": "${taxId.value}",
                 |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
                 |  "emailSource": "New",
                 |  "result": "Ok",
                 |  "authProviderId": "${TestData.ggCredId.value}"
                 |}""".stripMargin
                ).as[JsObject]
              )
              DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
                TestData.journeyId,
                StartEmailVerificationJourneyResult.Ok(TestData.emailVerificationRedirectUrl)
              )
            }
      }

    "audit correctly when there is no tax id in the journey and the selected email address is the same as the original bounced email" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(
        TestData.Journeys.SelectedEmail.journeyJson(taxId         = None, selectedEmail = TestData.bouncedEmail)
      )
      EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Success(TestData.emailVerificationRedirectUrl))
      AuditStub.audit()
      DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
        TestData.journeyId,
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson()
      )

      val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(TestData.emailVerificationRedirectUrl)

      EmailVerificationStub.verifyRequestEmailVerification(
        TestData.bouncedEmail,
        "http://localhost:12346/accessibility-statement/direct-debit-verify-email",
        "Check or change your Direct Debit email address",
        "en",
        "http://localhost:10801"
      )
      AuditStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.bouncedEmail.value.decryptedValue}",
             |  "emailSource": "Original",
             |  "result": "Ok",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
        TestData.journeyId,
        StartEmailVerificationJourneyResult.Ok(TestData.emailVerificationRedirectUrl)
      )
    }

    "pass in Welsh parameters to email verification if the user is navigating the service in Welsh" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
      EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Success(TestData.emailVerificationRedirectUrl))
      AuditStub.audit()
      DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
        TestData.journeyId,
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson()
      )

      val result = controller.requestVerification(
        TestData.fakeRequestWithAuthorization.withCookies(Cookie("PLAY_LANG", "cy"))
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(TestData.emailVerificationRedirectUrl)

      EmailVerificationStub.verifyRequestEmailVerification(
        TestData.selectedEmail,
        "http://localhost:12346/accessibility-statement/direct-debit-verify-email",
        "Gwirio neu newid eich cyfeiriad e-bost ar gyfer Debyd Uniongyrchol",
        "cy",
        "http://localhost:10801"
      )
      AuditStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "Ok",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
        TestData.journeyId,
        StartEmailVerificationJourneyResult.Ok(TestData.emailVerificationRedirectUrl)
      )
    }

    "must redirect to the email confirmed page if the email has already been verified" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
      EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Error(AlreadyVerified))
      AuditStub.audit()
      DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
        TestData.journeyId,
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.AlreadyVerified)
      )
      DirectDebitBackendStub.updateEmailAndBouncedFlag(TestData.ddiNumber)
      DirectDebitUpdateEmailBackendStub.updateEmailVerificationResult(
        TestData.journeyId,
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson()
      )

      val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.emailConfirmed.url)

      AuditStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "AlreadyVerified",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
        TestData.journeyId,
        StartEmailVerificationJourneyResult.AlreadyVerified
      )
      DirectDebitBackendStub.verifyUpdateEmailAndBouncedFlag(
        TestData.ddiNumber,
        TestData.selectedEmail,
        isBounced = false
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateEmailVerificationResult(
        TestData.journeyId,
        EmailVerificationResult.Verified
      )
    }

    "must return an error if the email address is already verified and there is an error updating the " +
      "email and bounced status flag" in {
        AuthStub.authorise()
        DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
        EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Error(AlreadyVerified))
        AuditStub.audit()
        DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
          TestData.journeyId,
          TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.AlreadyVerified)
        )
        DirectDebitBackendStub.updateEmailAndBouncedFlag(TestData.ddiNumber, INTERNAL_SERVER_ERROR)

        val error = intercept[UpstreamErrorResponse](
          await(controller.requestVerification(TestData.fakeRequestWithAuthorization))
        )
        error.statusCode shouldBe INTERNAL_SERVER_ERROR

        AuditStub.verifyEventAudited(
          "EmailVerificationRequested",
          Json.parse(
            s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "AlreadyVerified",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
          ).as[JsObject]
        )
        DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
          TestData.journeyId,
          StartEmailVerificationJourneyResult.AlreadyVerified
        )
        DirectDebitBackendStub.verifyUpdateEmailAndBouncedFlag(
          TestData.ddiNumber,
          TestData.selectedEmail,
          isBounced = false
        )
      }

    "must redirect to the too many passcode attempts page if the user has made too many passcode attempts" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
      EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Error(TooManyPasscodeAttempts))
      AuditStub.audit()
      DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
        TestData.journeyId,
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyPasscodeAttempts)
      )
      DirectDebitUpdateEmailBackendStub.updateEmailVerificationResult(
        TestData.journeyId,
        TestData.Journeys.ObtainedEmailVerificationResult.journeyJson(emailVerificationResult = EmailVerificationResult.Locked)
      )

      val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.tooManyPasscodeAttempts.url)

      AuditStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "TooManyPasscodeAttempts",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
        TestData.journeyId,
        StartEmailVerificationJourneyResult.TooManyPasscodeAttempts
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateEmailVerificationResult(
        TestData.journeyId,
        EmailVerificationResult.Locked
      )
    }

    "must redirect to the too many passcode journeys started page if the user has started too many " +
      "passcode journeys" in {
        AuthStub.authorise()
        DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
        EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Error(TooManyPasscodeJourneysStarted))
        AuditStub.audit()
        DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
          TestData.journeyId,
          TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted)
        )

        val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.tooManyPasscodeJourneysStarted.url)

        AuditStub.verifyEventAudited(
          "EmailVerificationRequested",
          Json.parse(
            s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "TooManyPasscodeJourneysStarted",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
          ).as[JsObject]
        )
        DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
          TestData.journeyId,
          StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted
        )
      }

    "must redirect to the too many different email addresses page if the user has tried to verify too " +
      "many email addresses" in {
        AuthStub.authorise()
        DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
        EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Error(TooManyDifferentEmailAddresses))
        AuditStub.audit()
        DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
          TestData.journeyId,
          TestData.Journeys.EmailVerificationJourneyStarted.journeyJson(startEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses)
        )

        val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.EmailVerificationResultController.tooManyDifferentEmailAddresses.url)

        AuditStub.verifyEventAudited(
          "EmailVerificationRequested",
          Json.parse(
            s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "TooManyDifferentEmailAddresses",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
          ).as[JsObject]
        )
        DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
          TestData.journeyId,
          StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses
        )
      }

  }

}

class EmailNotLocalControllerSpec extends ItSpec {

  override lazy val configOverrides: Map[String, Any] = Map(
    "platform.frontend.host" -> "https://platform-host"
  )

  lazy val controller = app.injector.instanceOf[EmailController]

  s"GET ${routes.EmailController.requestVerification.url}" - {

    "must redirect to the given redirectUrl if the verification journey has successfully started" in {
      val redirectUrl = "/redirect"

      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(TestData.Journeys.SelectedEmail.journeyJson())
      EmailVerificationStub.requestEmailVerification(StartEmailVerificationJourneyResponse.Success(redirectUrl))
      AuditStub.audit()
      DirectDebitUpdateEmailBackendStub.updateStartVerificationJourneyResult(
        TestData.journeyId,
        TestData.Journeys.EmailVerificationJourneyStarted.journeyJson()
      )

      val result = controller.requestVerification(TestData.fakeRequestWithAuthorization)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(redirectUrl)

      EmailVerificationStub.verifyRequestEmailVerification(
        TestData.selectedEmail,
        "/accessibility-statement/direct-debit-verify-email",
        "Check or change your Direct Debit email address",
        "en",
        ""
      )
      AuditStub.verifyEventAudited(
        "EmailVerificationRequested",
        Json.parse(
          s"""{
             |  "origin": "BTA",
             |  "taxType": "paye",
             |  "emailAddress": "${TestData.selectedEmail.value.decryptedValue}",
             |  "emailSource": "New",
             |  "result": "Ok",
             |  "authProviderId": "${TestData.ggCredId.value}"
             |}""".stripMargin
        ).as[JsObject]
      )
      DirectDebitUpdateEmailBackendStub.verifyUpdateStartVerificationJourneyResult(
        TestData.journeyId,
        StartEmailVerificationJourneyResult.Ok(TestData.emailVerificationRedirectUrl)
      )
    }

  }

}
