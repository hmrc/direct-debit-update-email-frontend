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

package uk.gov.hmrc.directdebitupdateemailfrontend.services

import cats.syntax.eq._
import ddUpdateEmail.models.journey.Journey
import ddUpdateEmail.models._
import play.api.libs.json._
import uk.gov.hmrc.directdebitupdateemailfrontend.models.audit.{AuditDetail, EmailSource, EmailVerificationRequestedAuditDetail, EmailVerificationResultAuditDetail}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  private val auditSource: String = "direct-debit-update-email-frontend"

  private def toAuditString(origin: Origin) = origin.toString.split('.').lastOption.getOrElse(origin.toString)

  private def audit[A <: AuditDetail: Writes](a: A)(implicit hc: HeaderCarrier): Unit = {
    val _ = auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = auditSource,
        auditType = a.auditType,
        eventId = UUID.randomUUID().toString,
        tags = hc.toAuditTags(),
        detail = Json.toJson(a)
      )
    )
  }

  def auditEmailVerificationRequested(
    journey:  Journey,
    ggCredId: GGCredId,
    email:    Email,
    result:   StartEmailVerificationJourneyResult
  )(implicit headerCarrier: HeaderCarrier): Unit =
    audit(toEmailVerificationRequested(journey, ggCredId, email, result))

  def auditEmailVerificationResult(
    journey:  Journey,
    ggCredId: GGCredId,
    email:    Email,
    result:   EmailVerificationResult
  )(implicit headerCarrier: HeaderCarrier): Unit =
    audit(toEmailVerificationResult(journey, ggCredId, email: Email, result))

  private def toEmailVerificationRequested(
    journey:  Journey,
    ggCredId: GGCredId,
    email:    Email,
    result:   StartEmailVerificationJourneyResult
  ): EmailVerificationRequestedAuditDetail = {
    val resultString =
      result match {
        case _: StartEmailVerificationJourneyResult.Ok                          => "Ok"
        case StartEmailVerificationJourneyResult.AlreadyVerified                => "AlreadyVerified"
        case StartEmailVerificationJourneyResult.TooManyPasscodeAttempts        => "TooManyPasscodeAttempts"
        case StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted => "TooManyPasscodeJourneysStarted"
        case StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses => "TooManyDifferentEmailAddresses"
      }

    EmailVerificationRequestedAuditDetail(
      origin = toAuditString(journey.origin),
      taxType = journey.taxRegime.entryName,
      taxId = journey.taxId.map(_.value),
      emailAddress = email.value.decryptedValue,
      emailSource = deriveEmailSource(journey, email),
      result = resultString,
      authProviderId = ggCredId.value
    )
  }

  private def toEmailVerificationResult(
    journey:  Journey,
    ggCredId: GGCredId,
    email:    Email,
    result:   EmailVerificationResult
  ): EmailVerificationResultAuditDetail = {
    val resultString = result match {
      case EmailVerificationResult.Verified => "Verified"
      case EmailVerificationResult.Locked   => "Locked"
    }

    EmailVerificationResultAuditDetail(
      origin = toAuditString(journey.origin),
      taxType = journey.taxRegime.entryName,
      taxId = journey.taxId.map(_.value),
      emailAddress = email.value.decryptedValue,
      emailSource = deriveEmailSource(journey, email),
      result = resultString,
      failureReason = result match {
        case EmailVerificationResult.Verified => None
        case EmailVerificationResult.Locked   => Some("TooManyPasscodeAttempts")
      },
      authProviderId = ggCredId.value
    )
  }

  private def deriveEmailSource(journey: Journey, selectedEmail: Email): EmailSource =
    if (journey.bouncedEmail === selectedEmail) EmailSource.Original else EmailSource.New

}
