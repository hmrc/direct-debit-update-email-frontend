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
import ddUpdateEmail.crypto.CryptoFormat
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.directdebitupdateemailfrontend.actions.Actions
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.connectors.EmailVerificationConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class EmailController @Inject() (
  actions:                    Actions,
  mcc:                        MessagesControllerComponents,
  emailVerificationConnector: EmailVerificationConnector
)(using ExecutionContext)
    extends FrontendController(mcc) {

  given CryptoFormat = CryptoFormat.NoOpCryptoFormat

  val emailVerificationPasscodes: Action[AnyContent] = actions.authenticatedJourneyAction.async { implicit request =>
    emailVerificationConnector
      .requestEmailVerification()
      .map(passcodes => Ok(Json.prettyPrint(Json.toJson(passcodes))))
  }

}
