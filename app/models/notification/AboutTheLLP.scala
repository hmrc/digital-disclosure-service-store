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

package models.notification

import play.api.libs.json.{Json, OFormat}
import models.address.Address
import crypto.EncryptedValue
import models.address.EncryptedAddress

final case class AboutTheLLP (
  name: Option[String] = None,
  address: Option[Address] = None
)

object AboutTheLLP {
  implicit val format: OFormat[AboutTheLLP] = Json.format[AboutTheLLP]
}

final case class EncryptedAboutTheLLP (
  name: Option[EncryptedValue] = None,
  address: Option[EncryptedAddress] = None
)

object EncryptedAboutTheLLP {
  implicit val format: OFormat[EncryptedAboutTheLLP] = Json.format[EncryptedAboutTheLLP]
}