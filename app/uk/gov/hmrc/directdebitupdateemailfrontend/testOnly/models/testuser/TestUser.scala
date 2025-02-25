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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.testuser

import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel}
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.forms.{SignInAs, StartJourneyForm}
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.utils.RandomDataGenerator

/** Definition of a test user. We use that data to log user in with defined enrolments.
  */
final case class TestUser(
  authorityId:     AuthorityId,
  affinityGroup:   AffinityGroup,
  confidenceLevel: ConfidenceLevel
)

object TestUser {

  def makeTestUser(form: StartJourneyForm): TestUser = {
    val affinityGroup: AffinityGroup = form.signInAs match {
      case SignInAs.Individual   => AffinityGroup.Individual
      case SignInAs.Organisation => AffinityGroup.Organisation
    }

    TestUser(
      authorityId = RandomDataGenerator.nextAuthorityId(),
      affinityGroup = affinityGroup,
      confidenceLevel = ConfidenceLevel.L50
    )

  }
}
