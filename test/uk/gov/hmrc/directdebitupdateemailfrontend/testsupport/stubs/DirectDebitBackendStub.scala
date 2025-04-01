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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import ddUpdateEmail.models.{DDINumber, Email}
import play.api.http.Status.NO_CONTENT

object DirectDebitBackendStub {

  private def updateEmailAndBouncedFlagUrl(ddiNumber: DDINumber) =
    s"/direct-debit-backend/bounced-email/status/${ddiNumber.value}"

  type HttpStatus = Int

  def updateEmailAndBouncedFlag(
    ddiNumber:      DDINumber,
    responseStatus: HttpStatus = NO_CONTENT
  ): StubMapping = stubFor(
    post(urlPathEqualTo(updateEmailAndBouncedFlagUrl(ddiNumber)))
      .willReturn(aResponse().withStatus(responseStatus))
  )

  def verifyUpdateEmailAndBouncedFlag(
    ddiNumber: DDINumber,
    email:     Email,
    isBounced: Boolean
  ): Unit =
    verify(
      postRequestedFor(urlPathEqualTo(updateEmailAndBouncedFlagUrl(ddiNumber)))
        .withRequestBody(
          equalToJson(
            s"""
           |{
           |  "email": "${email.value.decryptedValue}",
           |  "isBounced": ${isBounced.toString}
           |}
           |""".stripMargin
          )
        )
    )

}
