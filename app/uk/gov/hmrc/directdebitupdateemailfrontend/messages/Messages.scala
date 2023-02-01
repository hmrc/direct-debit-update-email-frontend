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

package uk.gov.hmrc.directdebitupdateemailfrontend.messages

import ddUpdateEmail.models.{Email, TaxRegime}

object Messages {

  val `Check or change your Direct Debit email address`: Message = Message(
    english = "Check or change your Direct Debit email address"
  )

  val continue: Message = Message(
    english = "Continue",
    welsh   = "Yn eich blaen"
  )

  val back: Message = Message(
    english = "Back",
    welsh   = "Yn ôl"
  )

  val `There is a problem`: Message = Message(
    english = "There is a problem",
    welsh   = "Mae problem wedi codi"
  )

  val error: Message = Message(
    english = "Error: ",
    welsh   = "Gwall: "
  )

  object ServicePhase {

    val beta: Message = Message(
      english = "beta",
      welsh   = "beta"
    )

    def bannerText(link: String): Message = Message(
      english = s"""This is a new service – your <a class="govuk-link" href="$link">feedback</a> will help us to improve it.""",
      welsh   = s"""Mae hwn yn wasanaeth newydd – bydd eich <a class="govuk-link" href="$link">adborth</a> yn ein helpu i’w wella."""
    )
  }

  object SelectEmail {

    val `Check or change you email address`: Message = Message(
      english = "Check or change you email address"
    )

    def `We cannot contact you`(taxRegime: TaxRegime, bouncedEmail: Email): Message = {
        def notHandledError: Message = sys.error("Tax regime not handled")

      taxRegime match {
        case TaxRegime.Paye =>
          Message(
            english = s"We cannot contact you about your Employers’ PAYE Direct Debit using ${bouncedEmail.value.decryptedValue}."
          )
        case TaxRegime.Zsdl => notHandledError
        case TaxRegime.VatC => notHandledError
        case TaxRegime.Cds  => notHandledError
        case TaxRegime.Ppt  => notHandledError
      }
    }

    val `The reason for this could be:`: Message = Message(
      english = "The reason for this could be:"
    )

    val `Your email inbox is full:`: Message = Message(
      english = "Your email inbox is full"
    )

    val `Your email address is not valid or it is spelt incorrectly`: Message = Message(
      english = "Your email address is not valid or it is spelt incorrectly"
    )

    val `Emails from HMRC have been marked as spam`: Message = Message(
      english = "Emails from HMRC have been marked as spam"
    )

    val `Which email address do you want to use?`: Message = Message(
      english = "Which email address do you want to use?"
    )

    val `Use a different email address`: Message = Message(
      english = "Use a different email address"
    )

    val `For example, myname@sample.com`: Message = Message(
      english = "For example, myname@sample.com"
    )

    def `Test ... with a verification email`(bouncedEmail: Email): Message = Message(
      english = s"Test ${bouncedEmail.value.decryptedValue} with a verification email"
    )

    def getError(key: String): Message = key match {
      case "selectAnEmailToUseRadio.error.required" =>
        Message(
          english = "Select which email address you want to use",
          welsh   = "Dewiswch pa gyfeiriad e-bost rydych chi am ei ddefnyddio"
        )

      case "newEmailInput.error.required" =>
        Message(
          english = "Enter your email address in the correct format, like name@example.com",
          welsh   = "Nodwch eich cyfeiriad e-bost yn y fformat cywir, megis enw@enghraifft.com"
        )

      case "newEmailInput.error.tooManyChar" =>
        Message(
          english = "Enter an email address with 256 characters or less",
          welsh   = "Nodwch gyfeiriad e-bost gan ddefnyddio 256 o gymeriadau neu lai"
        )

      case "newEmailInput.error.invalidFormat" =>
        Message(
          english = "Enter your email address in the correct format, like name@example.com",
          welsh   = "Nodwch eich cyfeiriad e-bost yn y fformat cywir, megis enw@enghraifft.com"
        )
    }

  }

}
