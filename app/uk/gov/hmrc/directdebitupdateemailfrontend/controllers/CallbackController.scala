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
import ddUpdateEmail.connectors.JourneyConnector
import ddUpdateEmail.models.EmailVerificationResult
import ddUpdateEmail.models.journey.Journey
import ddUpdateEmail.utils.Errors
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.directdebitupdateemailfrontend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailfrontend.services.{DirectDebitBackendService, EmailVerificationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CallbackController @Inject() (
    actions:                   Actions,
    journeyConnector:          JourneyConnector,
    emailVerificationService:  EmailVerificationService,
    directDebitBackendService: DirectDebitBackendService,
    mcc:                       MessagesControllerComponents
)(implicit ec: ExecutionContext) extends FrontendController(mcc) {

  val callback: Action[AnyContent] = actions.authenticatedJourneyAction.async { implicit request =>
    request.journey match {
      case j: Journey.BeforeEmailVerificationJourneyStarted =>
        Errors.throwServerErrorException(
          s"Required email address to be selected but got journey in state ${j.getClass.getSimpleName}: journeyId = ${j._id.value}"
        )

      case j: Journey.AfterEmailVerificationJourneyStarted =>
        val selectedEmail = j match {
          case j: Journey.AfterSelectedEmail => j.selectedEmail
        }
        val result = for {
          verificationResult <- emailVerificationService.getVerificationResult(selectedEmail)
          _ <- verificationResult match {
            case EmailVerificationResult.Verified =>
              directDebitBackendService.updateEmailAndBouncedFlag(j.sjRequest.ddiNumber, selectedEmail, isBounced = false)
            case EmailVerificationResult.Locked =>
              Future.successful(())
          }
          _ <- journeyConnector.updateEmailVerificationResult(j._id, verificationResult)
        } yield verificationResult

        result.map {
          case EmailVerificationResult.Verified => Redirect(routes.EmailVerificationResultController.emailConfirmed)
          case EmailVerificationResult.Locked   => Redirect(routes.EmailVerificationResultController.tooManyPasscodeAttempts)
        }

    }
  }

}
