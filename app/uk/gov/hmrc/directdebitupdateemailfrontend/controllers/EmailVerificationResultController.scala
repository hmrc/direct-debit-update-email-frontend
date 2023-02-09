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

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.{EmailVerificationResult, StartEmailVerificationJourneyResult}
import ddUpdateEmail.models.journey.Journey.{AfterEmailVerificationJourneyStarted, AfterEmailVerificationResult, BeforeEmailVerificationJourneyStarted, BeforeEmailVerificationResult}
import ddUpdateEmail.utils.Errors
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.directdebitupdateemailfrontend.actions.Actions
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton
class EmailVerificationResultController @Inject() (
    actions: Actions,
    mcc:     MessagesControllerComponents
) extends FrontendController(mcc) {

  val emailConfirmed: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationResult =>
        Errors.throwServerErrorException("Cannot show email confirmed page before email verification result has been obtained. " +
          s"Journey is in state ${j.getClass.getSimpleName}")

      case j: AfterEmailVerificationResult =>
        j.emailVerificationResult match {
          case EmailVerificationResult.Verified =>
            Ok("placeholder for email confirmed page")

          case EmailVerificationResult.Locked =>
            Errors.throwServerErrorException("Cannot show email confirmed page when email verification result is 'Locked'")
        }
    }
  }

  val tooManyPasscodeAttempts: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationResult =>
        Errors.throwServerErrorException("Cannot show tooManyPasscodeAttempts page before email verification result has been obtained. " +
          s"Journey is in state ${j.getClass.getSimpleName}")

      case j: AfterEmailVerificationResult =>
        j.emailVerificationResult match {
          case EmailVerificationResult.Verified =>
            Errors.throwServerErrorException("Cannot show tooManyPasscodeAttempts page when email verification result is 'Verified'")

          case EmailVerificationResult.Locked =>
            Ok("placeholder for too many passcodes page")
        }
    }
  }

  val tooManyPasscodeJourneysStarted: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationJourneyStarted =>
        Errors.throwServerErrorException("Cannot show tooManyPasscodeJourneysStarted page before email verification journey has been started. " +
          s"Journey is in state ${j.getClass.getSimpleName}")

      case j: AfterEmailVerificationJourneyStarted =>
        j.startEmailVerificationJourneyResult match {
          case StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted =>
            Ok("placeholder for too many passcode journeys started page")

          case other =>
            Errors.throwServerErrorException("Cannot show tooManyPasscodeJourneysStarted when start verification journey result " +
              s"is ${other.getClass.getSimpleName}")
        }

    }
  }

  val tooManyDifferentEmailAddresses: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationJourneyStarted =>
        Errors.throwServerErrorException("Cannot show tooManyDifferentEmailAddresses page before email verification journey has been started. " +
          s"Journey is in state ${j.getClass.getSimpleName}")

      case j: AfterEmailVerificationJourneyStarted =>
        j.startEmailVerificationJourneyResult match {
          case StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses =>
            Ok("placeholder for too many different email addresses page")

          case other =>
            Errors.throwServerErrorException("Cannot show tooManyDifferentEmailAddresses when start verification journey result " +
              s"is ${other.getClass.getSimpleName}")
        }

    }
  }

}
