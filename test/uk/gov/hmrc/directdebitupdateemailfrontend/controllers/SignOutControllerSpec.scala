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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.FakeRequestUtils.FakeRequestOps
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.{ContentAssertions, ItSpec}

import scala.concurrent.Future

class SignOutControllerSpec extends ItSpec {

  private val controller: SignOutController = app.injector.instanceOf[SignOutController]

  "signOut should" - {
    val signOutUrl = "https://www.gov.uk"
    s"redirect to $signOutUrl" in {
      val result: Future[Result] = controller.signOut(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(signOutUrl)

    }
  }

  "signOutFromTimeout should" - {

    "return the timed out page" in {

      val request                = TestData.fakeRequestWithAuthorization
      val result: Future[Result] = controller.signOutFromTimeout(request)
      val pageContent: String    = contentAsString(result)
      val doc: Document          = Jsoup.parse(pageContent)

      status(result) shouldBe OK
      ContentAssertions.commonPageChecks(
        doc,
        expectedH1 = "For your security, we signed you out",
        expectedSubmitUrl = None,
        hasBackLink = false,
        hasSignOutLink = false
      )(request)
      val signInButton = doc.select(".govuk-body")
      signInButton.text() shouldBe "Sign in"
      signInButton.select("a").attr("href") shouldBe "http://localhost:9949/auth-login-stub/gg-sign-in"
    }

    "return the timed out page in Welsh" in {

      val request                = TestData.fakeRequestWithAuthorization.withLang(Language.Welsh)
      val result: Future[Result] = controller.signOutFromTimeout(request)
      val pageContent: String    = contentAsString(result)
      val doc: Document          = Jsoup.parse(pageContent)

      status(result) shouldBe OK
      ContentAssertions.commonPageChecks(
        doc,
        expectedH1 = "Er eich diogelwch, gwnaethom eich allgofnodi",
        expectedSubmitUrl = None,
        hasBackLink = false,
        hasSignOutLink = false,
        language = Language.Welsh
      )(request)
      val signInButton = doc.select(".govuk-body")
      signInButton.text() shouldBe "Mewngofnodi"
      signInButton.select("a").attr("href") shouldBe "http://localhost:9949/auth-login-stub/gg-sign-in"
    }

  }

}
