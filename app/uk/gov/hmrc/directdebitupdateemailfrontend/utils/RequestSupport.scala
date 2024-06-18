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

package uk.gov.hmrc.directdebitupdateemailfrontend.utils

import play.api.i18n._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language
import uk.gov.hmrc.http.SessionKeys

import javax.inject.Inject

/**
 * I'm repeating a pattern which was brought originally by play-framework
 * and putting some more data which can be derived from a request
 *
 * Use it to provide HeaderCarrier, Lang, or Messages
 */
class RequestSupport @Inject() (i18nSupport: I18nSupport) {

  implicit def language(implicit requestHeader: RequestHeader): Language = {
    val lang: Lang = i18nSupport.request2Messages(requestHeader).lang
    Language(lang)
  }

  implicit def legacyMessages(implicit requestHeader: RequestHeader): Messages = {
    i18nSupport.request2Messages(requestHeader)
  }

  def isLoggedIn(implicit request: RequestHeader): Boolean = request.session.get(SessionKeys.authToken).isDefined

}

