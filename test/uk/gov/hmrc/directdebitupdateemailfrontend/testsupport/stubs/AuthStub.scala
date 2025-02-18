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

package uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData

object AuthStub {

  def authorise(
    credentials: Option[Credentials] = Some(Credentials(TestData.ggCredId.value, "GovernmentGateway"))
  ): StubMapping = {
    val authoriseJsonBody = credentials.fold(
      Json.obj()
    )(credential =>
      Json.obj(
        "optionalCredentials" -> Json.obj(
          "providerId"   -> credential.providerId,
          "providerType" -> credential.providerType
        )
      )
    )

    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.prettyPrint(authoriseJsonBody))
        )
    )
  }

}
