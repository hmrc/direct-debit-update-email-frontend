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

package uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata

import ddUpdateEmail.models.journey.{Journey, JourneyId, SessionId, SjRequest}
import ddUpdateEmail.models._
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.{Encrypter, PlainText}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.FakeRequestUtils._

import java.time.Instant

object TestData {

  val ggCredId: GGCredId = GGCredId("cred-123")

  val authToken = "authorization-value"

  val sessionId = SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")

  val ddiNumber: DDINumber = DDINumber("0123456789")

  val fakeRequestWithAuthorization = FakeRequest().withAuthToken(authToken).withSessionId(sessionId.value)

  val journeyId: JourneyId = JourneyId("b6217497-ab5b-4e93-855a-afc9f9e933b6")

  val sjRequest: SjRequest = SjRequest(
    ddiNumber,
    BackUrl("/back"),
    ReturnUrl("/return")
  )

  val bouncedEmail: Email = Email(SensitiveString("bounced@email.com"))

  val selectedEmail: Email = Email(SensitiveString("selected@email.com"))

  val emailVerificationRedirectUrl = "/redirect"

  val frozenInstantString: String = "2057-11-02T16:28:55.185Z"

  val frozenInstant: Instant = Instant.parse(frozenInstantString)

  def encryptString(s: String)(implicit encrypter: Encrypter): String =
    encrypter.encrypt(
      PlainText("\"" + SensitiveString(s).decryptedValue + "\"")
    ).value

  object Journeys {

    private def taxIdJsonString(taxId: Option[TaxId]) =
      taxId match {
        case Some(taxId) =>
          val taxType = taxId match {
            case _: TaxId.EmpRef => "EmpRef"
            case _: TaxId.Vrn    => "Vrn"
            case _: TaxId.Zppt   => "Zppt"
            case _: TaxId.Zsdl   => "Zsdl"
          }
          s"""
             |    "taxId": {
             |      "$taxType": {
             |        "value": "${taxId.value}"
             |      }
             |    },
             |""".stripMargin

        case None => ""
      }

    object Started {

      def journey(origin: Origin = Origin.BTA, taxRegime: TaxRegime = TaxRegime.Paye, taxId: Option[TaxId] = None): Journey.Started =
        Journey.Started(
          journeyId,
          origin,
          frozenInstant,
          sjRequest,
          sessionId,
          taxRegime,
          taxId,
          bouncedEmail
        )

      def journeyJson(origin: String = "BTA", taxRegime: String = "paye", taxId: Option[TaxId] = None)(implicit encrypter: Encrypter): String =
        s"""{
          |  "Started": {
          |    "_id": "${journeyId.value}",
          |    "bouncedEmail": "${encryptString(bouncedEmail.value.decryptedValue)}",
          |    "taxRegime": "$taxRegime",${taxIdJsonString(taxId)}
          |    "sessionId": "${sessionId.value}",
          |    "createdOn": "$frozenInstantString",
          |    "origin": "$origin",
          |    "sjRequest":{
          |      "ddiNumber": "${sjRequest.ddiNumber.value}",
          |      "backUrl": "${sjRequest.backUrl.value}",
          |      "returnUrl": "${sjRequest.returnUrl.value}"
          |    },
          |    "sessionId": "${sessionId.value}",
          |    "createdAt":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
          |    "lastUpdated":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } }
          |  }
          |}""".stripMargin

    }

    object SelectedEmail {

      def journey(
          origin:        Origin        = Origin.BTA,
          taxRegime:     TaxRegime     = TaxRegime.Paye,
          selectedEmail: Email         = selectedEmail,
          taxId:         Option[TaxId] = None
      ): Journey.SelectedEmail =
        Journey.SelectedEmail(
          journeyId,
          origin,
          frozenInstant,
          sjRequest,
          sessionId,
          taxRegime,
          taxId,
          bouncedEmail,
          selectedEmail
        )

      def journeyJson(
          origin:        String        = "BTA",
          taxRegime:     String        = "paye",
          selectedEmail: Email         = selectedEmail,
          taxId:         Option[TaxId] = None
      )(implicit encrypter: Encrypter): String =
        s"""{
           |  "SelectedEmail": {
           |    "_id": "${journeyId.value}",
           |    "bouncedEmail": "${encryptString(bouncedEmail.value.decryptedValue)}",
           |    "taxRegime": "$taxRegime",${taxIdJsonString(taxId)}
           |    "sessionId": "${sessionId.value}",
           |    "createdOn": "$frozenInstantString",
           |    "origin": "$origin",
           |    "sjRequest":{
           |      "ddiNumber": "${sjRequest.ddiNumber.value}",
           |      "backUrl": "${sjRequest.backUrl.value}",
           |      "returnUrl": "${sjRequest.returnUrl.value}"
           |    },
           |    "sessionId": "${sessionId.value}",
           |    "createdAt":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
           |    "lastUpdated":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
           |    "selectedEmail": "${encryptString(selectedEmail.value.decryptedValue)}"
           |  }
           |}""".stripMargin

    }

    object EmailVerificationJourneyStarted {

      def journey(
          origin:                              Origin                              = Origin.BTA,
          taxRegime:                           TaxRegime                           = TaxRegime.Paye,
          selectedEmail:                       Email                               = selectedEmail,
          startEmailVerificationJourneyResult: StartEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.Ok(emailVerificationRedirectUrl),
          taxId:                               Option[TaxId]                       = None
      ): Journey.EmailVerificationJourneyStarted =
        Journey.EmailVerificationJourneyStarted(
          journeyId,
          origin,
          frozenInstant,
          sjRequest,
          sessionId,
          taxRegime,
          taxId,
          bouncedEmail,
          selectedEmail,
          startEmailVerificationJourneyResult
        )

      def journeyJson(
          origin:                              String                              = "BTA",
          taxRegime:                           String                              = "paye",
          selectedEmail:                       Email                               = selectedEmail,
          startEmailVerificationJourneyResult: StartEmailVerificationJourneyResult = StartEmailVerificationJourneyResult.Ok(emailVerificationRedirectUrl),
          taxId:                               Option[TaxId]                       = None
      )(implicit encrypter: Encrypter): String = {
        s"""{
           |  "EmailVerificationJourneyStarted": {
           |    "_id": "${journeyId.value}",
           |    "bouncedEmail": "${encryptString(bouncedEmail.value.decryptedValue)}",
           |    "taxRegime": "$taxRegime",${taxIdJsonString(taxId)}
           |    "sessionId": "${sessionId.value}",
           |    "createdOn": "$frozenInstantString",
           |    "origin": "$origin",
           |    "sjRequest":{
           |      "ddiNumber": "${sjRequest.ddiNumber.value}",
           |      "backUrl": "${sjRequest.backUrl.value}",
           |      "returnUrl": "${sjRequest.returnUrl.value}"
           |    },
           |    "sessionId": "${sessionId.value}",
           |    "createdAt":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
           |    "lastUpdated":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
           |    "selectedEmail": "${encryptString(selectedEmail.value.decryptedValue)}",
           |    "startEmailVerificationJourneyResult": ${startResultJsonValue(startEmailVerificationJourneyResult)}
           |  }
           |}""".stripMargin
      }

    }

    object ObtainedEmailVerificationResult {

      def journey(
          origin:                  Origin                  = Origin.BTA,
          taxRegime:               TaxRegime               = TaxRegime.Paye,
          selectedEmail:           Email                   = selectedEmail,
          emailVerificationResult: EmailVerificationResult = EmailVerificationResult.Verified,
          taxId:                   Option[TaxId]           = None
      ): Journey.ObtainedEmailVerificationResult =
        Journey.ObtainedEmailVerificationResult(
          journeyId,
          origin,
          frozenInstant,
          sjRequest,
          sessionId,
          taxRegime,
          taxId,
          bouncedEmail,
          selectedEmail,
          StartEmailVerificationJourneyResult.Ok(emailVerificationRedirectUrl),
          emailVerificationResult
        )

      def journeyJson(
          origin:                  String                  = "BTA",
          taxRegime:               String                  = "paye",
          selectedEmail:           Email                   = selectedEmail,
          emailVerificationResult: EmailVerificationResult = EmailVerificationResult.Verified,
          taxId:                   Option[TaxId]           = None
      )(implicit encrypter: Encrypter): String = {
        s"""{
           |  "ObtainedEmailVerificationResult": {
           |    "_id": "${journeyId.value}",
           |    "bouncedEmail": "${encryptString(bouncedEmail.value.decryptedValue)}",
           |    "taxRegime": "$taxRegime",${taxIdJsonString(taxId)}
           |    "sessionId": "${sessionId.value}",
           |    "createdOn": "$frozenInstantString",
           |    "origin": "$origin",
           |    "sjRequest":{
           |      "ddiNumber": "${sjRequest.ddiNumber.value}",
           |      "backUrl": "${sjRequest.backUrl.value}",
           |      "returnUrl": "${sjRequest.returnUrl.value}"
           |    },
           |    "sessionId": "${sessionId.value}",
           |    "createdAt":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
           |    "lastUpdated":{"$$date": {"$$numberLong": "${frozenInstant.toEpochMilli.toString}" } },
           |    "selectedEmail": "${encryptString(selectedEmail.value.decryptedValue)}",
           |    "startEmailVerificationJourneyResult": ${startResultJsonValue(StartEmailVerificationJourneyResult.Ok(emailVerificationRedirectUrl))},
           |    "emailVerificationResult": "${emailVerificationResult.entryName}"
           |  }
           |}""".stripMargin
      }

    }

    def startResultJsonValue(startEmailVerificationJourneyResult: StartEmailVerificationJourneyResult): String = startEmailVerificationJourneyResult match {
      case StartEmailVerificationJourneyResult.Ok(redirect) =>
        s"""{
           |  "Ok": {
           |    "redirectUrl": "${redirect}"
           |  }
           |}
           |""".stripMargin

      case other =>
        s"""{
           |  "${other.toString}": {}
           |}
           |""".stripMargin
    }

  }

}
