/*
 * Copyright 2024 HM Revenue & Customs
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

package models.notification

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}
import models.YesNoOrUnsure
import models.address.Address
import crypto.EncryptedValue
import models.address.EncryptedAddress

final case class AboutYou(
  fullName: Option[String] = None,
  telephoneNumber: Option[String] = None,
  emailAddress: Option[String] = None,
  dateOfBirth: Option[LocalDate] = None,
  mainOccupation: Option[String] = None,
  contactPreference: Option[ContactPreferences] = None,
  doYouHaveANino: Option[YesNoOrUnsure] = None,
  nino: Option[String] = None,
  registeredForVAT: Option[YesNoOrUnsure] = None,
  vatRegNumber: Option[String] = None,
  registeredForSA: Option[YesNoOrUnsure] = None,
  sautr: Option[String] = None,
  address: Option[Address] = None
)

object AboutYou {
  implicit val format: OFormat[AboutYou] = Json.format[AboutYou]
}

final case class EncryptedAboutYou(
  fullName: Option[EncryptedValue] = None,
  telephoneNumber: Option[EncryptedValue] = None,
  emailAddress: Option[EncryptedValue] = None,
  dateOfBirth: Option[EncryptedValue] = None,
  mainOccupation: Option[String] = None,
  contactPreference: Option[ContactPreferences] = None,
  doYouHaveANino: Option[YesNoOrUnsure] = None,
  nino: Option[EncryptedValue] = None,
  registeredForVAT: Option[YesNoOrUnsure] = None,
  vatRegNumber: Option[EncryptedValue] = None,
  registeredForSA: Option[YesNoOrUnsure] = None,
  sautr: Option[EncryptedValue] = None,
  address: Option[EncryptedAddress] = None
)

object EncryptedAboutYou {
  implicit val format: OFormat[EncryptedAboutYou] = Json.format[EncryptedAboutYou]
}
