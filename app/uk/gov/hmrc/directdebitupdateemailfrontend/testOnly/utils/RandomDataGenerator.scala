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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.utils

import ddUpdateEmail.models.DDINumber
import uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.testuser.AuthorityId

import java.util.UUID
import scala.util.Random

object RandomDataGenerator {

  private implicit val random: Random.type = Random

  /**
   * Next n-digit number string. Values can start from '0'
   */
  def nextNumber(n: Int): String = random.alphanumeric.filter(_.isDigit).take(n).mkString

  def nextAlphanumeric(n: Int): String = random.alphanumeric.take(n).mkString

  def nextAlpha(n: Int): String = random.alphanumeric.filter(_.isLetter).take(n).mkString

  def nextEmpref(): String = {
    val nextTaxOfficeNumber = nextNumber(3)
    val nextTaxOfficeReference = s"GZ${nextNumber(5)}"
    s"$nextTaxOfficeNumber$nextTaxOfficeReference"
  }

  def nextCdsRef(): String = nextNumber(7)

  def nextZsdlRef(): String = nextAlphanumeric(15)

  def nextVrn(): String = nextNumber(9)

  def nextZpptRef(): String = s"X${nextAlpha(1)}PPT000${nextNumber(7)}"

  def nextDdiNumber(): DDINumber = DDINumber(nextNumber(18))

  def nextAuthorityId(): AuthorityId = AuthorityId(s"authId-${UUID.randomUUID().toString}")

}
