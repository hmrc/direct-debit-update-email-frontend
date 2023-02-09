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

import cats.syntax.eq._
import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.connectors.JourneyConnector
import ddUpdateEmail.models.{Email, EmailVerificationResult, StartEmailVerificationJourneyResult}
import ddUpdateEmail.models.journey.Journey
import ddUpdateEmail.utils.Errors
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.directdebitupdateemailfrontend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailfrontend.controllers.EmailController.ChooseEmailForm
import uk.gov.hmrc.directdebitupdateemailfrontend.services.EmailVerificationService
import uk.gov.hmrc.emailaddress.EmailAddress
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.directdebitupdateemailfrontend.views.html
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailController @Inject() (
    actions:                  Actions,
    selectEmailPage:          html.SelectEmail,
    journeyConnector:         JourneyConnector,
    emailVerificationService: EmailVerificationService,
    mcc:                      MessagesControllerComponents
)(implicit ec: ExecutionContext) extends FrontendController(mcc) {

  val selectEmail: Action[AnyContent] = actions.authenticatedJourneyAction { implicit request =>
    val form = existingSelectedEmail(request.journey).fold(
      EmailController.chooseEmailForm()
    ) { selectedEmail =>
        val chooseEmailFormData =
          if (selectedEmail === request.journey.bouncedEmail)
            ChooseEmailForm(request.journey.bouncedEmail.value.decryptedValue, None)
          else
            ChooseEmailForm(request.journey.bouncedEmail.value.decryptedValue, Some(selectedEmail.value.decryptedValue))

        EmailController.chooseEmailForm().fill(chooseEmailFormData)
      }

    Ok(selectEmailPage(request.journey.taxRegime, request.journey.bouncedEmail, request.journey.sjRequest.backUrl, form))
  }

  private def existingSelectedEmail(journey: Journey): Option[Email] =
    journey match {
      case _: Journey.BeforeSelectedEmail => None
      case j: Journey.AfterSelectedEmail  => Some(j.selectedEmail)
    }

  val selectEmailSubmit: Action[AnyContent] = actions.authenticatedJourneyAction.async { implicit request =>
    EmailController.chooseEmailForm()
      .bindFromRequest()
      .fold(
        formWithErrors =>
          BadRequest(
            selectEmailPage(request.journey.taxRegime, request.journey.bouncedEmail, request.journey.sjRequest.backUrl, formWithErrors)
          ),
        { formData =>
          val selectedEmail = formData.differentEmail.map(e => Email(SensitiveString(e))).getOrElse(request.journey.bouncedEmail)
          journeyConnector.updateSelectedEmail(request.journeyId, selectedEmail).map(_ =>
            Redirect(routes.EmailController.requestVerification))
        }
      )
  }

  val requestVerification: Action[AnyContent] = actions.authenticatedJourneyAction.async{ implicit request =>
    request.journey match {
      case j: Journey.BeforeSelectedEmail =>
        Errors.throwServerErrorException(
          s"Required email address to be selected but got journey in state ${j.getClass.getSimpleName}: journeyId = ${j._id.value}"
        )

      case j: Journey.AfterSelectedEmail =>
        val result = for {
          startResult <- emailVerificationService.startEmailVerificationJourney(j.selectedEmail)
          updatedJourney <- journeyConnector.updateStartEmailVerificationJourneyResult(j._id, startResult)
          _ <- {
            // bring the journey forward to ObtainedEmailVerificationResult if already verified or locked
            startResult match {
              case StartEmailVerificationJourneyResult.AlreadyVerified =>
                journeyConnector.updateEmailVerificationResult(j._id, EmailVerificationResult.Verified)

              case StartEmailVerificationJourneyResult.TooManyPasscodeAttempts =>
                journeyConnector.updateEmailVerificationResult(j._id, EmailVerificationResult.Locked)

              case _ =>
                Future.successful(updatedJourney)
            }
          }
        } yield startResult

        result.map {
          case StartEmailVerificationJourneyResult.Ok(redirectUrl) =>
            Redirect(redirectUrl)
          case StartEmailVerificationJourneyResult.AlreadyVerified =>
            Redirect(routes.EmailVerificationResultController.emailConfirmed)

          case StartEmailVerificationJourneyResult.TooManyPasscodeAttempts =>
            Redirect(routes.EmailVerificationResultController.tooManyPasscodeAttempts)

          case StartEmailVerificationJourneyResult.TooManyPasscodeJourneysStarted =>
            Redirect(routes.EmailVerificationResultController.tooManyPasscodeJourneysStarted)

          case StartEmailVerificationJourneyResult.TooManyDifferentEmailAddresses =>
            Redirect(routes.EmailVerificationResultController.tooManyDifferentEmailAddresses)
        }
    }
  }

}

object EmailController {

  final case class ChooseEmailForm(email: String, differentEmail: Option[String])

  def chooseEmailForm(): Form[ChooseEmailForm] = Form(
    mapping(
      "selectAnEmailToUseRadio" -> nonEmptyText,
      "newEmailInput" -> mandatoryIfEqual("selectAnEmailToUseRadio", "new", differentEmailAddressMapping)
    )(ChooseEmailForm.apply)(ChooseEmailForm.unapply)
  )

  val differentEmailAddressMapping: Mapping[String] = nonEmptyText
    .transform[String](email => email.toLowerCase(Locale.UK), _.toLowerCase(Locale.UK))
    .verifying(
      Constraint[String]((email: String) =>
        if (email.length > 256) Invalid("error.tooManyChar")
        else if (EmailAddress.isValid(email)) Valid
        else Invalid("error.invalidFormat"))
    )

}
