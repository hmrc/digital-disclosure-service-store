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

package models.address

import play.api.libs.json.Json
import play.api.libs.json.OFormat
import crypto.EncryptedValue

final case class Country(
  code: String
) {
  val messageKey: String = s"country.$code"
}

object Country {
  implicit val countryFormat: OFormat[Country] = Json.format[Country]
}

final case class EncryptedCountry(
  code: EncryptedValue
)

object EncryptedCountry {
  implicit val countryFormat: OFormat[EncryptedCountry] = Json.format[EncryptedCountry]
}