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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class EmailController @Inject() (
    journeyConnector: JourneyConnector,
    mcc:              MessagesControllerComponents
)(implicit ec: ExecutionContext) extends FrontendController(mcc) {

  val selectEmail: Action[AnyContent] = Action.async { implicit request =>
    journeyConnector.findLatestJourneyBySessionId().map{ journey =>
      Ok(s"Placeholder for select email page.\n\nJourney is ${journey.toString}.\n\nHeaderCarrier is ${hc(request).toString}")
    }

  }

}
