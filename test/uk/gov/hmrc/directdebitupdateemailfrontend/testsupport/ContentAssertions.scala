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

package uk.gov.hmrc.directdebitupdateemailfrontend.testsupport

import cats.syntax.eq._
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import play.api.mvc.Request
import uk.gov.hmrc.directdebitupdateemailfrontend.controllers.routes
import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

object ContentAssertions extends RichMatchers {

  def assertListOfContent(elements: Elements)(expectedContent: List[String]) =
    elements.asScala.toList
      .zip(expectedContent)
      .map { case (element, expectedText) => element.text() shouldBe expectedText }

  // used for summary lists
  def assertKeyAndValue(element: Element, keyValue: (String, String)): Assertion = {
    element.select(".govuk-summary-list__key").text() shouldBe keyValue._1
    element.select(".govuk-summary-list__value").text() shouldBe keyValue._2
  }

  def languageToggleExists(document: Document, selectedLanguage: Language): Assertion = {
    val langToggleItems: List[Element] = document.select(".hmrc-language-select__list-item").asScala.toList
    langToggleItems.size shouldBe 2

    val englishOption = langToggleItems(0)
    val welshOption   = langToggleItems(1)

    selectedLanguage match {
      case Language.English =>
        englishOption.text() shouldBe "English"

        welshOption.select("a").attr("hreflang") shouldBe "cy"
        welshOption.select("span.govuk-visually-hidden").text() shouldBe "Newid yr iaith i’r Gymraeg"
        welshOption.select("span[aria-hidden=true]").text() shouldBe "Cymraeg"

      case Language.Welsh =>
        englishOption.select("a").attr("hreflang") shouldBe "en"
        englishOption.select("span.govuk-visually-hidden").text() shouldBe "Change the language to English"
        englishOption.select("span[aria-hidden=true]").text() shouldBe "English"

        welshOption.text() shouldBe "Cymraeg"

    }

  }

  @nowarn
  def commonPageChecks(
    page:                Document,
    expectedH1:          String,
    expectedSubmitUrl:   Option[String],
    hasFormError:        Boolean = false,
    language:            Language = Language.English,
    backLinkOverrideUrl: Option[String] = None,
    hasBackLink:         Boolean = true,
    hasSignOutLink:      Boolean = true
  )(implicit request: Request[_]): Unit = {
    val titlePrefix = if (hasFormError) {
      language match {
        case Language.English => "Error: "
        case Language.Welsh   => "Gwall: "
      }
    } else ""

    val expectedServiceName = language match {
      case Language.English => "Check or change your Direct Debit email address"
      case Language.Welsh   => "Gwirio neu newid eich cyfeiriad e-bost ar gyfer Debyd Uniongyrchol"
    }

    page.title() shouldBe s"$titlePrefix$expectedH1 - $expectedServiceName - GOV.UK"

    val serviceName = page.select("a.govuk-header__service-name")
    serviceName.text() shouldBe expectedServiceName
    serviceName.attr("href") shouldBe routes.EmailController.selectEmail.url

    page.select("h1").text() shouldBe expectedH1

    ContentAssertions.languageToggleExists(page, language)

    val backLink = page.select(".govuk-back-link")
    if (hasBackLink) {
      backLink.text shouldBe (language match {
        case Language.English => "Back"
        case Language.Welsh   => "Yn ôl"
      })

      backLinkOverrideUrl match {
        case Some(url) =>
          backLink.hasClass("js-visible") shouldBe false
          backLink.attr("href") shouldBe url

        case None =>
          backLink.hasClass("js-visible") shouldBe true
          backLink.attr("href") shouldBe "#"
      }
    } else backLink.isEmpty shouldBe true

    val signOutLink = page.select(".hmrc-sign-out-nav__link")
    if (hasSignOutLink) {
      signOutLink.text() shouldBe (language match {
        case Language.English => "Sign out"
        case Language.Welsh   => "Allgofnodi"
      })
      signOutLink.attr("href") shouldBe
        s"http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:10801/direct-debit-verify-email/sign-out"
    }

    val betaLink = page.select(".govuk-phase-banner__text").select("a").attr("href")
    betaLink shouldBe "http://localhost:9250/contact/beta-feedback?service=direct-debit-frontend"

    if (hasFormError) {
      val expectedText = language match {
        case Language.English => "Error:"
        case Language.Welsh   => "Gwall:"
      }
      page.select(".govuk-error-message > .govuk-visually-hidden").text shouldBe expectedText
    }

    val footerLinks = page.select(".govuk-footer__link").asScala.toList

    val accessibilityStatementLinkText = language match {
      case Language.English => "Accessibility statement"
      case Language.Welsh   => "Datganiad hygyrchedd"
    }
    val accessibilityStatementLink     = footerLinks.find(_.text().eqv(accessibilityStatementLinkText))
    accessibilityStatementLink.map(_.attr("href")) shouldBe Some(
      "http://localhost:12346/accessibility-statement/direct-debit-verify-email?referrerUrl=%2F"
    )

    val form = page.select("form")
    expectedSubmitUrl match {
      case None         => form.isEmpty shouldBe true
      case Some(submit) =>
        form.attr("action") shouldBe submit
        form.select(".govuk-button").text shouldBe (language match {
          case Language.English => "Continue"
          case Language.Welsh   => "Yn eich blaen"
        })
    }

  }

}
