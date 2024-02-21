/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.directdebitupdateemailfrontend.config.AppConfig
import uk.gov.hmrc.directdebitupdateemailfrontend.views.html.PageUnavailable
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class PageUnavailableController @Inject() (
    mcc:                 MessagesControllerComponents,
    pageUnavailablePage: PageUnavailable,
    appConfig:           AppConfig
) extends FrontendController(mcc) {

  def pageUnavailable: Action[AnyContent] = Action { implicit request =>
    Ok(pageUnavailablePage(appConfig.Urls.businessTaxAccountUrl)).withNewSession //[OPS-11673] kick-out so new session needed
  }

}
