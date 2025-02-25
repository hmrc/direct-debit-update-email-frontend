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

import ddUpdateEmail.connectors.JourneyConnector
import ddUpdateEmail.models.GGCredId
import ddUpdateEmail.models.journey.{Journey, JourneyId}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Request, Result}
import uk.gov.hmrc.directdebitupdateemailfrontend.controllers.routes
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedJourneyRequest[A](
  override val request: Request[A],
  val journey:          Journey,
  ggCredId:             GGCredId
) extends AuthenticatedRequest[A](request, ggCredId) {
  val journeyId: JourneyId = journey._id
}

@Singleton
class GetJourneyActionRefiner @Inject() (
  journeyConnector: JourneyConnector
)(using ec: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, AuthenticatedJourneyRequest],
      FrontendHeaderCarrierProvider,
      Logging {

  override protected def refine[A](
    request: AuthenticatedRequest[A]
  ): Future[Either[Result, AuthenticatedJourneyRequest[A]]] = {
    given Request[A] = request
    for {
      maybeJourney: Option[Journey] <- journeyConnector.findLatestJourneyBySessionId()
    } yield maybeJourney match {
      case Some(journey) =>
        Right(new AuthenticatedJourneyRequest(request, journey, request.ggCredId))
      case None          =>
        logger.warn(s"No journey found for sessionId: ${hc.sessionId.map(_.value).getOrElse("-")}")
        Left(Redirect(routes.PageUnavailableController.pageUnavailable))
    }
  }

  override protected def executionContext: ExecutionContext = ec

}
