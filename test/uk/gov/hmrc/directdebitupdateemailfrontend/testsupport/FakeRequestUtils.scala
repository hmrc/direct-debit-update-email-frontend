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

package uk.gov.hmrc.directdebitupdateemailfrontend.testsupport

import play.api.mvc.Cookie
import play.api.test.FakeRequest
import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language
import uk.gov.hmrc.http.SessionKeys

object FakeRequestUtils {

  implicit class FakeRequestOps[T](r: FakeRequest[T]) {
    def withLang(lang: Language): FakeRequest[T] = r.withCookies(Cookie("PLAY_LANG", lang.code))

    def withAuthToken(authToken: String): FakeRequest[T] = r.withSession((SessionKeys.authToken, authToken))

    def withSessionId(sessionId: String): FakeRequest[T] = r.withSession(SessionKeys.sessionId -> sessionId)
  }
}
