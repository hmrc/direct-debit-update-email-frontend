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

package uk.gov.hmrc.directdebitupdateemailfrontend.messages

import uk.gov.hmrc.directdebitupdateemailfrontend.models.Language

final case class Message private (
  english: String,
  welsh:   Option[String]
) {

  def show(implicit language: Language): String = language match {
    case Language.English => english
    case Language.Welsh   => welsh.getOrElse(english)
  }

}

object Message {

  private def apply(english: String, welsh: Option[String]) = new Message(english, welsh)

  def apply(english: String, welsh: String): Message = Message(english, Option(welsh))

  def apply(english: String): Message = Message(english, None)
}
