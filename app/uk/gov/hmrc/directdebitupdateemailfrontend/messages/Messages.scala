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
    english = "Check or change your Direct Debit email address",
    welsh   = "Gwirio neu newid eich cyfeiriad e-bost ar gyfer Debyd Uniongyrchol"
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

  object Date {

    val monthName: Map[Int, Message] = Map(
      1 -> Message(english = "January", welsh = "Ionawr"),
      2 -> Message(english = "February", welsh = "Chwefror"),
      3 -> Message(english = "March", welsh = "Mawrth"),
      4 -> Message(english = "April", welsh = "Ebrill"),
      5 -> Message(english = "May", welsh = "Mai"),
      6 -> Message(english = "June", welsh = "Mehefin"),
      7 -> Message(english = "July", welsh = "Gorffennaf"),
      8 -> Message(english = "August", welsh = "Awst"),
      9 -> Message(english = "September", welsh = "Medi"),
      10 -> Message(english = "October", welsh = "Hydref"),
      11 -> Message(english = "November", welsh = "Tachwedd"),
      12 -> Message(english = "December", welsh = "Rhagfyr")
    )

  }

  val `Sign in`: Message = Message(
    english = "Sign in",
    welsh   = "Mewngofnodi"
  )

  object ServicePhase {

    val beta: Message = Message(
      english = "Beta",
      welsh   = "Beta"
    )

    def bannerText(link: String): Message = Message(
      english = s"""This is a new service – your <a class="govuk-link" href="$link">feedback</a> will help us to improve it.""",
      welsh   = s"""Mae hwn yn wasanaeth newydd – bydd eich <a class="govuk-link" href="$link">adborth</a> yn ein helpu i’w wella."""
    )
  }

  object SelectEmail {

    val `Check or change your email address`: Message = Message(
      english = "Check or change your email address",
      welsh   = "Gwirio neu newid eich cyfeiriad e-bost"
    )

    def `We cannot contact you`(taxRegime: TaxRegime, bouncedEmail: Email): Message = {
        def notHandledError: Message = sys.error("Tax regime not handled")

      taxRegime match {
        case TaxRegime.Paye =>
          Message(
            english = s"We cannot contact you about your Employers’ PAYE Direct Debit using ${bouncedEmail.value.decryptedValue}.",
            welsh   = s"Ni allwn gysylltu â chi am eich Debyd Uniongyrchol ar gyfer TWE y Cyflogwr gan ddefnyddio ${bouncedEmail.value.decryptedValue}."
          )
        case TaxRegime.Zsdl => notHandledError
        case TaxRegime.VatC => notHandledError
        case TaxRegime.Cds  => notHandledError
        case TaxRegime.Ppt  => notHandledError
      }
    }

    val `The reason for this could be:`: Message = Message(
      english = "The reason for this could be:",
      welsh   = "Gallai’r rheswm am hyn fod y naill o’r canlynol:"
    )

    val `Your email inbox is full:`: Message = Message(
      english = "Your email inbox is full",
      welsh   = "Mae mewnflwch eich e-bost yn llawn"
    )

    val `Your email address is not valid or it is spelt incorrectly`: Message = Message(
      english = "Your email address is not valid or it is spelt incorrectly",
      welsh   = "Mae cyfeiriad eich e-bost yn annilys neu heb gael ei sillafu’n gywir"
    )

    val `Emails from HMRC have been marked as spam`: Message = Message(
      english = "Emails from HMRC have been marked as spam",
      welsh   = "Mae e-byst gan CThEF wedi’u nodi fel sbam"
    )

    val `Which email address do you want to use?`: Message = Message(
      english = "Which email address do you want to use?",
      welsh   = "Pa gyfeiriad e-bost ydych chi am ei ddefnyddio?"
    )

    val `Use a different email address`: Message = Message(
      english = "Use a different email address",
      welsh   = "Defnyddio cyfeiriad e-bost gwahanol"
    )

    val `Email address`: Message = Message(
      english = "Email address",
      welsh   = "Cyfeiriad e-bost"
    )

    val `For example, myname@sample.com`: Message = Message(
      english = "For example, myname@sample.com",
      welsh   = "Er enghraifft, fyenw@enghraifft.cymru"
    )

    def `Test ... with a verification email`(bouncedEmail: Email): Message = Message(
      english = s"Test ${bouncedEmail.value.decryptedValue} with a verification email",
      welsh   = s"Profi ${bouncedEmail.value.decryptedValue} gydag e-bost dilysu"
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

  object EmailConfirmed {

    val `Email address verified`: Message = Message(
      english = "Email address verified",
      welsh   = "Cyfeiriad e-bost wedi’i ddilysu"
    )

    def `We'll use ... to contact you about your Direct debit`(email: Email): Message = Message(
      english = s"We’ll use <strong>${email.value.decryptedValue}</strong> to contact you about your Direct Debit.",
      welsh   = s"Byddwn yn defnyddio <strong>${email.value.decryptedValue}</strong> i gysylltu â chi ynghylch eich Debyd Uniongyrchol."
    )

    val `Your email address has not been changed in other government services`: Message = Message(
      english = "Your email address has not been changed in other government services.",
      welsh   = "Nid yw’ch e-bost wedi cael ei newid ar gyfer gwasanaethau eraill y llywodraeth."
    )

  }

  object TimeOut {

    val `For your security, we signed you out`: Message = Message(
      english = "For your security, we signed you out",
      welsh   = "Er eich diogelwch, gwnaethom eich allgofnodi"
    )

    val `You’re about to be signed out`: Message = Message(
      english = "You’re about to be signed out",
      welsh   = "Rydych ar fin cael eich allgofnodi"
    )

  }

  object TooManyEmailAddresses {

    val `You have tried to verify too many email addresses`: Message = Message(
      english = "You have tried to verify too many email addresses",
      welsh   = "Rydych wedi ceisio dilysu gormod o gyfeiriadau e-bost"
    )

    def `You have been locked out because you have tried to verify too many email addresses`(date: String, time: String): Message = Message(
      english = s"""You have been locked out because you have tried to verify too many email addresses. Please try again on <strong>$date at $time</strong>.""",
      welsh   = s"""Rydych chi wedi cael eich cloi allan oherwydd eich bod wedi ceisio dilysu gormod o gyfeiriadau e-bost. Rhowch gynnig arall arni ar <strong>$date am $time</strong>."""
    )

    val `Return to tax account`: Message = Message(
      english = "Return to tax account",
      welsh   = "Yn ôl i’r cyfrif treth"
    )

  }

  object TooManyPasscodes {

    val `Email verification code entered too many times`: Message = Message(
      english = "Email verification code entered too many times",
      welsh   = "Cod dilysu e-bost wedi’i nodi gormod o weithiau"
    )

    val `You have entered an email verification code too many times.`: Message = Message(
      english = "You have entered an email verification code too many times.",
      welsh   = "Rydych chi wedi nodi cod dilysu e-bost gormod o weithiau."
    )

    def `You can go back to enter a new email address`(url: String): Message = Message(
      english = s"""You can <a class="govuk-link" href="$url">go back to enter a new email address</a>.""",
      welsh   = s"""Gallwch <a class="govuk-link" href="$url">fynd yn ôl i nodi cyfeiriad e-bost newydd</a>."""
    )

  }

  object TooManyPasscodeJourneysStarted {

    val `You have tried to verify an email address too many times`: Message = Message(
      english = "You have tried to verify an email address too many times",
      welsh   = "Rydych wedi ceisio dilysu cyfeiriad e-bost gormod o weithiau"
    )

    def `You have tried to verify... too many times.`(email: Email): Message = Message(
      english = s"You have tried to verify <strong>${email.value.decryptedValue}</strong> too many times.",
      welsh   = s"Rydych wedi ceisio dilysu <strong>${email.value.decryptedValue}</strong> gormod o weithiau."
    )

    def `You will need to verify a different email address.`(url: String): Message = Message(
      english = s"""You will need to <a class="govuk-link" href="$url">verify a different email address</a>.""",
      welsh   = s"""Bydd angen i chi <a class="govuk-link" href="$url">ddilysu cyfeiriad e-bost gwahanol</a>."""
    )

  }

  object PageUnavailable {

    val `Sorry, the page is unavailable`: Message = Message(
      english = "Sorry, the page is unavailable",
      welsh   = "Mae’n ddrwg gennym, nid yw’r dudalen ar gael"
    )

    val `The page is unavailable.`: Message = Message(
      english = "The page is unavailable.",
      welsh   = "Nid yw’r dudalen ar gael."
    )

    def `Go to your tax account to check or change your Direct Debit email address.`(url: String): Message = Message(
      english = s"""<a class="govuk-link" href="$url">Go to your tax account</a> to check or change your Direct Debit email address.""",
      welsh   = s"""<a class="govuk-link" href="$url">Ewch i’ch cyfrif treth</a> i wirio neu i newid eich cyfeiriad e-bost ar gyfer Debyd Uniongyrchol."""
    )

  }

}
