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

package uk.gov.hmrc.directdebitupdateemailfrontend.actions

import com.google.inject.{Inject, Singleton}
import ddUpdateEmail.models.GGCredId
import ddUpdateEmail.utils.Errors
import play.api.mvc.Results.Redirect
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.directdebitupdateemailfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedRequest[A](
  val request:  Request[A],
  val ggCredId: GGCredId
) extends WrappedRequest[A](request)

class AuthenticatedActionRefiner @Inject() (
  val authConnector: AuthConnector,
  appConfig:         AppConfig,
  cc:                MessagesControllerComponents
) extends ActionRefiner[Request, AuthenticatedRequest],
      AuthorisedFunctions,
      FrontendHeaderCarrierProvider {

  private given ExecutionContext = cc.executionContext

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    given Request[A] = request

    authorised(AuthProviders(GovernmentGateway))
      .retrieve(
        Retrievals.credentials
      ) {
        case None =>
          Future.failed(new RuntimeException(s"Could not find credentials"))

        case Some(ggCredId) =>
          Future.successful(
            Right(new AuthenticatedRequest[A](request, GGCredId(ggCredId.providerId)))
          )

      }
      .recover {
        case _: NoActiveSession        => Left(redirectToLoginPage(request))
        case e: AuthorisationException =>
          Errors.throwServerErrorException(s"Unauthorised because of ${e.reason}, please investigate why")
      }
  }

  private def redirectToLoginPage(request: Request[?]): Result =
    Redirect(
      appConfig.BaseUrl.gg,
      Map(
        "continue" -> Seq(appConfig.BaseUrl.ddUpdateEmailFrontend + request.uri),
        "origin"   -> Seq("direct-debit-update-email-frontend")
      )
    )

  override protected def executionContext: ExecutionContext = cc.executionContext

}
