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

package models

import java.time.Instant
import models.notification._
import models.disclosure._
import play.api.libs.json.{Json, OFormat}

sealed trait EncryptedSubmission {
  def userId: String
  def submissionId: String
  def lastUpdated: Instant
}

object EncryptedSubmission {
  implicit val format: OFormat[EncryptedSubmission] = Json.format[EncryptedSubmission]
}

final case class EncryptedNotification (
  userId: String,
  submissionId: String,
  lastUpdated: Instant,
  created: Instant,
  metadata: Metadata,
  personalDetails: EncryptedPersonalDetails,
  customerId: Option[CustomerId] = None,
  madeDeclaration: Boolean = false
) extends EncryptedSubmission

object EncryptedNotification {
  implicit val format: OFormat[EncryptedNotification] = Json.format[EncryptedNotification]
}

final case class EncryptedFullDisclosure (
  userId: String,
  submissionId: String,
  lastUpdated: Instant,
  created: Instant,
  metadata: Metadata,
  caseReference: EncryptedCaseReference,
  personalDetails: EncryptedPersonalDetails,
  offshoreLiabilities: OffshoreLiabilities,
  otherLiabilities: OtherLiabilities,
  reasonForDisclosingNow: EncryptedReasonForDisclosingNow,
  customerId: Option[CustomerId] = None,
  madeDeclaration: Boolean = false
) extends EncryptedSubmission

object EncryptedFullDisclosure {
  implicit val format: OFormat[EncryptedFullDisclosure] = Json.format[EncryptedFullDisclosure]
}