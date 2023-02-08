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
import ddUpdateEmail.models.journey.Journey
import ddUpdateEmail.utils.Errors
import paymentsEmailVerification.models.EmailVerificationResult
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.directdebitupdateemailfrontend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailfrontend.services.EmailVerificationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class CallbackController @Inject() (
    actions:                  Actions,
    emailVerificationService: EmailVerificationService,
    mcc:                      MessagesControllerComponents
)(implicit ec: ExecutionContext) extends FrontendController(mcc) {

  val callback: Action[AnyContent] = actions.authenticatedJourneyAction.async { implicit request =>
    request.journey match {
      case j: Journey.BeforeSelectedEmail =>
        Errors.throwServerErrorException(
          s"Required email address to be selected but got journey in stage ${j.stage.toString}: journeyId = ${j._id.value}"
        )

      case j: Journey.AfterSelectedEmail =>
        emailVerificationService.getVerificationResult(j.selectedEmail).map {
          case EmailVerificationResult.Verified => Redirect(routes.EmailVerificationResultController.emailConfirmed)
          case EmailVerificationResult.Locked   => Redirect(routes.EmailVerificationResultController.tooManyPasscodeAttempts)
        }

    }
  }

}
