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
import ddUpdateEmail.models.journey.JourneyId
import ddUpdateEmail.models.{Email, EmailVerificationResult, StartEmailVerificationJourneyResult}
import play.api.http.Status._
import uk.gov.hmrc.crypto.Encrypter
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.testdata.TestData
import uk.gov.hmrc.http.HeaderNames

object DirectDebitUpdateEmailBackendStub {

  private val baseUrl: String = "/direct-debit-update-email/journey"

  private val findByLatestSessionIdUrl: String = s"$baseUrl/find-latest-by-session-id"

  private def updateSelectedEmailUrl(journeyId: JourneyId): String = s"$baseUrl/${journeyId.value}/selected-email"
  private def updateStartVerificationJourneyResultUrl(journeyId: JourneyId): String = s"$baseUrl/${journeyId.value}/start-verification-journey-result"
  private def updateEmailVerificationResultUrl(journeyId: JourneyId): String = s"$baseUrl/${journeyId.value}/email-verification-result"

  def findByLatestSessionId(jsonBody: Option[String]): StubMapping = stubFor(
    get(urlPathEqualTo(findByLatestSessionIdUrl))
      .willReturn{
        jsonBody.fold(aResponse().withStatus(NOT_FOUND)){
          json =>
            aResponse().withStatus(OK).withBody(json)
        }
      }
  )

  def findByLatestSessionId(jsonBody: String): StubMapping = findByLatestSessionId(Some(jsonBody))

  def verifyFindByLatestSessionId(sessionId: String = TestData.sessionId.value): Unit =
    verify(
      getRequestedFor(urlPathEqualTo(findByLatestSessionIdUrl))
        .withHeader(HeaderNames.xSessionId, equalTo(sessionId))
    )

  def updateSelectedEmail(journeyId: JourneyId, responseJsonBody: String) = stubFor(
    post(urlPathEqualTo(updateSelectedEmailUrl(journeyId)))
      .willReturn(aResponse().withStatus(OK).withBody(responseJsonBody))
  )

  def updateStartVerificationJourneyResult(journeyId: JourneyId, responseJsonBody: String) = stubFor(
    post(urlPathEqualTo(updateStartVerificationJourneyResultUrl(journeyId)))
      .willReturn(aResponse().withStatus(OK).withBody(responseJsonBody))
  )

  def updateEmailVerificationResult(journeyId: JourneyId, responseJsonBody: String) = stubFor(
    post(urlPathEqualTo(updateEmailVerificationResultUrl(journeyId)))
      .willReturn(aResponse().withStatus(OK).withBody(responseJsonBody))
  )

  def verifyUpdateSelectedEmail(journeyId: JourneyId, selectedEmail: Email)(implicit encrypter: Encrypter) =
    verify(
      postRequestedFor(urlPathEqualTo(updateSelectedEmailUrl(journeyId)))
        .withRequestBody(equalToJson(s""" "${TestData.encryptString(selectedEmail.value.decryptedValue)}" """))
    )

  def verifyUpdateStartVerificationJourneyResult(journeyId: JourneyId, startResult: StartEmailVerificationJourneyResult) =
    verify(
      postRequestedFor(urlPathEqualTo(updateStartVerificationJourneyResultUrl(journeyId)))
        .withRequestBody(equalToJson(TestData.Journeys.startResultJsonValue(startResult)))
    )

  def verifyUpdateEmailVerificationResult(journeyId: JourneyId, result: EmailVerificationResult) =
    verify(
      postRequestedFor(urlPathEqualTo(updateEmailVerificationResultUrl(journeyId)))
        .withRequestBody(equalToJson(s""" "${result.toString}" """))
    )

}
