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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.controllers

import com.google.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.forms.StartJourneyForm
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.services.StartService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.views.html._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

@Singleton
class StartJourneyController @Inject() (
    startPage:      StartPage,
    startErrorPage: StartErrorPage,
    backPage:       IAmABackPage,
    returnPage:     IAmAReturnPage,
    startService:   StartService,
    mcc:            MessagesControllerComponents

)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) {

  val startJourney: Action[AnyContent] = Action { implicit request =>
    Ok(startPage(StartJourneyForm.form))
  }

  val startJourneySubmit: Action[AnyContent] = Action.async { implicit request =>
    StartJourneyForm.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(startPage(formWithErrors))),
        startForm => startService.start(startForm).map{
          case Left(error)            => InternalServerError(startErrorPage(error))
          case Right((session, next)) => Redirect(next.value).withSession(session)
        }
      )
  }

  val dummyBack: Action[AnyContent] = Action { implicit request =>
    Ok(backPage())
  }

  val dummyReturn: Action[AnyContent] = Action { implicit request =>
    Ok(returnPage())
  }

}
