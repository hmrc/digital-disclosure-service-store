/*
 * Copyright 2022 HM Revenue & Customs
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
import java.time.Instant

final case class Notification (
  userId: String,
  notificationId: String,
  lastUpdated: Instant,
  metadata: Metadata,
  background: Background,
  aboutYou: AboutYou,
  aboutTheIndividual: Option[AboutTheIndividual] = None,
  aboutTheCompany: Option[AboutTheCompany] = None,
  aboutTheTrust: Option[AboutTheTrust] = None,
  aboutTheLLP: Option[AboutTheLLP] = None,
  aboutTheEstate: Option[AboutTheEstate] = None,
  customerId: Option[CustomerId] = None
)

object Notification {
  implicit val format: OFormat[Notification] = Json.format[Notification]
}

final case class EncryptedNotification (
  userId: String,
  notificationId: String,
  lastUpdated: Instant,
  metadata: Metadata,
  background: EncryptedBackground,
  aboutYou: EncryptedAboutYou,
  aboutTheIndividual: Option[EncryptedAboutTheIndividual] = None,
  aboutTheCompany: Option[EncryptedAboutTheCompany] = None,
  aboutTheTrust: Option[EncryptedAboutTheTrust] = None,
  aboutTheLLP: Option[EncryptedAboutTheLLP] = None,
  aboutTheEstate: Option[EncryptedAboutTheEstate] = None,
  customerId: Option[CustomerId] = None
)

object EncryptedNotification {
  implicit val format: OFormat[EncryptedNotification] = Json.format[EncryptedNotification]
}