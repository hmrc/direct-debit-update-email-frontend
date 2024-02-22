/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.DocumentUtils.DocumentOps
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.{ContentAssertions, ItSpec}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.Future

class PageUnavailableControllerSpec extends ItSpec with LogCapturing {

  private val controller: PageUnavailableController = app.injector.instanceOf[PageUnavailableController]

  "pageUnavailable" - {
    "display the page unavailable page" in {
      val result: Future[Result] = controller.pageUnavailable(FakeRequest())
      status(result) shouldBe OK

      val pageContent: String = contentAsString(result)
      val doc: Document = Jsoup.parse(pageContent)
      ContentAssertions.commonPageChecks(
        doc,
        expectedH1        = "Sorry, the page is unavailable",
        expectedSubmitUrl = None,
        hasBackLink       = false,
        hasSignOutLink    = false
      )(FakeRequest())

      val paragraphs = doc.selectList("p.govuk-body")
      paragraphs.size shouldBe 2
      paragraphs(0).text() shouldBe "The page is unavailable."
      paragraphs(1).text() shouldBe "Go to your tax account to check or change your Direct Debit email address."
      val link = doc.select("p > a.govuk-link")
      link.text() shouldBe "Go to your tax account"

      link.attr("href") should contain("/business-account")
    }
  }
}
