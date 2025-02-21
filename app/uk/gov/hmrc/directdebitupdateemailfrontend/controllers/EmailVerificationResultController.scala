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
import ddUpdateEmail.models.journey.Journey.{AfterEmailVerificationJourneyStarted, AfterEmailVerificationResult, AfterSelectedEmail, BeforeEmailVerificationJourneyStarted, BeforeEmailVerificationResult}
import ddUpdateEmail.models.{EmailVerificationResult, StartEmailVerificationJourneyResult}
import ddUpdateEmail.utils.Errors
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.directdebitupdateemailfrontend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailfrontend.services.EmailVerificationService
import uk.gov.hmrc.directdebitupdateemailfrontend.views.html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class EmailVerificationResultController @Inject() (
  actions:                     Actions,
  emailVerificationService:    EmailVerificationService,
  emailConfirmedPage:          html.EmailConfirmed,
  tooManyEmailAddressesPage:   html.TooManyEmailAddresses,
  tooManyPasscodesPage:        html.TooManyPasscodes,
  tooManyPasscodeJourneysPage: html.TooManyPasscodeJourneysStarted,
  mcc:                         MessagesControllerComponents
)(using ExecutionContext)
    extends FrontendController(mcc) {

  val emailConfirmed: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationResult =>
        Errors.throwServerErrorException(
          "Cannot show email confirmed page before email verification result has been obtained. " +
            s"Journey is in state ${j.getClass.getSimpleName}"
        )

      case j: AfterEmailVerificationResult =>
        j.emailVerificationResult match {
          case EmailVerificationResult.Verified =>
            val selectedEmail = j match {
              case j1: AfterSelectedEmail => j1.selectedEmail
            }

            Ok(emailConfirmedPage(selectedEmail, request.journey.sjRequest.returnUrl))

          case EmailVerificationResult.Locked =>
            Errors.throwServerErrorException(
              "Cannot show email confirmed page when email verification result is 'Locked'"
            )
        }
    }
  }

  val tooManyPasscodeAttempts: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationResult =>
        Errors.throwServerErrorException(
          "Cannot show tooManyPasscodeAttempts page before email verification result has been obtained. " +
            s"Journey is in state ${j.getClass.getSimpleName}"
        )

      case j: AfterEmailVerificationResult =>
        j.emailVerificationResult match {
          case EmailVerificationResult.Verified =>
            Errors.throwServerErrorException(
              "Cannot show tooManyPasscodeAttempts page when email verification result is 'Verified'"
            )

          case EmailVerificationResult.Locked =>
            Ok(tooManyPasscodesPage())
        }
    }
  }

  val tooManyPasscodeJourneysStarted: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    request.journey match {
      case j: BeforeEmailVerificationJourneyStarted =>
        Errors.throwServerErrorException(
          "Cannot show tooManyPasscodeJourneysStarted page before email verification journey has been started. " +
            s"Journey is in state ${j.getClass.getSimpleName}"
        )

      case j: AfterEmailVerificationJourneyStarted =>
        j.startEmailVerificationJourneyResult match {
          case StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted =>
            val selectedEmail = j match {
              case j1: AfterSelectedEmail => j1.selectedEmail
            }
            Ok(tooManyPasscodeJourneysPage(selectedEmail))

          case other =>
            Errors.throwServerErrorException(
              "Cannot show tooManyPasscodeJourneysStarted when start verification journey result " +
                s"is ${other.getClass.getSimpleName}"
            )
        }

    }
  }

  val tooManyDifferentEmailAddresses: Action[AnyContent] = actions.authenticatedJourneyAction.async {
    implicit request =>
      request.journey match {
        case j: BeforeEmailVerificationJourneyStarted =>
          Errors.throwServerErrorException(
            "Cannot show tooManyDifferentEmailAddresses page before email verification journey has been started. " +
              s"Journey is in state ${j.getClass.getSimpleName}"
          )

        case j: AfterEmailVerificationJourneyStarted =>
          j.startEmailVerificationJourneyResult match {
            case StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses =>
              emailVerificationService.getEarliestCreatedAtTime().map {
                _.earliestCreatedAtTime.fold(
                  Errors.throwServerErrorException("Could not find earliest created at time")
                )(earliestCreatedAtTime =>
                  Ok(tooManyEmailAddressesPage(earliestCreatedAtTime.plusDays(1L), j.sjRequest.backUrl))
                )
              }

            case other =>
              Errors.throwServerErrorException(
                "Cannot show tooManyDifferentEmailAddresses when start verification journey result " +
                  s"is ${other.getClass.getSimpleName}"
              )
          }

      }
  }

}
