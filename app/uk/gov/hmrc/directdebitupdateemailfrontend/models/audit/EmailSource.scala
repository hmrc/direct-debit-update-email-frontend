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

package uk.gov.hmrc.directdebitupdateemailfrontend.models.audit

import ddUpdateEmail.utils.EnumFormat
import enumeratum.{EnumEntry, PlayEnum}
import play.api.libs.json.Format

import scala.collection.immutable

sealed trait EmailSource extends EnumEntry

object EmailSource extends PlayEnum[EmailSource] {

  case object New extends EmailSource

  case object Original extends EmailSource

  implicit val format: Format[EmailSource] = EnumFormat(EmailSource)

  override val values: immutable.IndexedSeq[EmailSource] = findValues

}