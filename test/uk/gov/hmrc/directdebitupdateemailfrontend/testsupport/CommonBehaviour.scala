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

import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs.{AuthStub, DirectDebitUpdateEmailBackendStub}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData

trait CommonBehaviour { this: ItSpec =>

  def authenticatedJourneyBehaviour(action: Action[AnyContent]): Unit = {

    "must be an authenticated endpoint" in {
      val result = action(FakeRequest("GET", "/abc"))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        "http://localhost:9949/auth-login-stub/gg-sign-in?" +
          "continue=http%3A%2F%2Flocalhost%3A10801%2Fabc" +
          "&origin=direct-debit-update-email-frontend"
      )
    }

    "must redirect if no journey can be found" in {
      AuthStub.authorise()
      DirectDebitUpdateEmailBackendStub.findByLatestSessionId(None)

      val result = action(TestData.fakeRequestWithAuthorization)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/direct-debit-verify-email/page-unavailable")
      DirectDebitUpdateEmailBackendStub.verifyFindByLatestSessionId()
    }
  }

}
