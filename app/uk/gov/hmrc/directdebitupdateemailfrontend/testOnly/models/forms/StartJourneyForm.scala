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

package uk.gov.hmrc.directdebitupdateemailfrontend.testOnly.models.forms

import ddUpdateEmail.models.{Email, Origin, TaxRegime}
import play.api.data.Forms.{boolean, mapping, text}
import play.api.data._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.directdebitupdateemailfrontend.controllers.EmailController
import uk.gov.hmrc.directdebitupdateemailfrontend.utils.EnumFormatter

import java.util.Locale

final case class StartJourneyForm(
  signInAs:  SignInAs,
  origin:    Origin,
  taxRegime: TaxRegime,
  email:     Email,
  isBounced: Boolean
)

object StartJourneyForm {

  val form: Form[StartJourneyForm] = {

    val signInAsKey: String  = "signInAs"
    val originKey            = "origin"
    val taxRegimeKey: String = "taxRegime"
    val emailKey: String     = "email"
    val isBouncedKey: String = "isEmailBounced"

    val signInMapping: Mapping[SignInAs] = Forms.of(
      EnumFormatter.format(
        `enum` = SignInAs,
        errorMessageIfMissing = "Select how to be signed in",
        errorMessageIfEnumError = "Select how to be signed in"
      )
    )

    val originMapping: FieldMapping[Origin] = Forms.of(
      EnumFormatter.format(
        `enum` = Origin,
        errorMessageIfMissing = "Select which origin the journey should start from",
        errorMessageIfEnumError = "Select which origin the journey should start from"
      )
    )

    val taxRegimeFormatter: FieldMapping[TaxRegime] = Forms.of(
      EnumFormatter.format(
        `enum` = TaxRegime,
        errorMessageIfMissing = "Select a tax regime",
        errorMessageIfEnumError = "Select a tax regime",
        insensitive = true
      )
    )

    val emailAddressMapping: Mapping[Email] = text
      .transform[String](email => email.toLowerCase(Locale.UK), _.toLowerCase(Locale.UK))
      .verifying(EmailController.emailConstraint)
      .transform(s => Email(SensitiveString(s)), _.value.decryptedValue)

    Form(
      mapping(
        signInAsKey  -> signInMapping,
        originKey    -> originMapping,
        taxRegimeKey -> taxRegimeFormatter,
        emailKey     -> emailAddressMapping,
        isBouncedKey -> boolean
      )(StartJourneyForm.apply)(f =>
        Some(
          (
            f.signInAs,
            f.origin,
            f.taxRegime,
            f.email,
            f.isBounced
          )
        )
      )
    )
  }
}
