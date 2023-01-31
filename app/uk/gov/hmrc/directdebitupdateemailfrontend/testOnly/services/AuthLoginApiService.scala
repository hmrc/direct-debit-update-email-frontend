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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.services

import com.google.inject.{Inject, Singleton}
import play.api.http.HeaderNames
import play.api.libs.json.{JsNull, JsObject, Json}
import play.api.mvc.Session
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.testuser.{AuthToken, TestUser}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, SessionKeys}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Clock
import java.util.UUID.randomUUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Test Login Service.
 */
@Singleton
class AuthLoginApiService @Inject() (
    httpClient:     HttpClient,
    servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {

  def logIn(testUser: TestUser): Future[Session] = for {
    authToken: AuthToken <- callAuthLoginApi(makeLoginRequestBody(testUser))
  } yield buildAuthenticatedSession(authToken)

  private def makeLoginRequestBody(testUser: TestUser): JsObject =
    Json.obj(
      "credId" -> testUser.authorityId.value,
      "affinityGroup" -> testUser.affinityGroup.toString,
      "confidenceLevel" -> testUser.confidenceLevel,
      "credentialStrength" -> "weak",
      "credentialRole" -> "User",
      "usersName" -> JsNull,
      "enrolments" -> Json.arr(),
      "delegatedEnrolments" -> Json.arr(),
      "email" -> "user@test.com",
      "gatewayInformation" -> Json.obj()
    )

  private def callAuthLoginApi(requestBody: JsObject): Future[AuthToken] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    httpClient.POST[JsObject, HttpResponse](s"$authLoginApiUrl/government-gateway/session/login", requestBody).map(r =>
      AuthToken(
        r
          .header(HeaderNames.AUTHORIZATION)
          .getOrElse(throw new RuntimeException(s"missing 'AUTHORIZATION' header: ${r.toString()}"))
      ))
  }

  private def buildAuthenticatedSession(authToken: AuthToken) =
    Session(Map(
      SessionKeys.sessionId -> s"session-${randomUUID.toString}",
      SessionKeys.authToken -> authToken.value,
      SessionKeys.lastRequestTimestamp -> realClock.millis().toString
    ))

  private val realClock: Clock = Clock.systemUTC()

  private val authLoginApiUrl = servicesConfig.baseUrl("auth-login-api")
}

