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

package models.store

import java.time.Instant
import models.notification._
import models.disclosure._
import play.api.libs.json.{Json, OFormat}
import models.{CustomerId, Metadata}

sealed trait Submission {
  def userId: String
  def submissionId: String
  def lastUpdated: Instant
  def created: Instant
}

object Submission {
  implicit val format: OFormat[Submission] = Json.using[Json.WithDefaultValues].format[Submission]
}

final case class Notification(
  userId: String,
  submissionId: String,
  lastUpdated: Instant,
  created: Instant = Instant.now(),
  metadata: Metadata,
  personalDetails: PersonalDetails,
  customerId: Option[CustomerId] = None,
  madeDeclaration: Boolean = false
) extends Submission

object Notification {
  implicit val format: OFormat[Notification] = Json.using[Json.WithDefaultValues].format[Notification]
}

final case class FullDisclosure(
  userId: String,
  submissionId: String,
  lastUpdated: Instant,
  created: Instant = Instant.now(),
  metadata: Metadata,
  caseReference: CaseReference,
  personalDetails: PersonalDetails,
  offshoreLiabilities: OffshoreLiabilities,
  onshoreLiabilities: Option[OnshoreLiabilities] = None,
  otherLiabilities: OtherLiabilities,
  reasonForDisclosingNow: ReasonForDisclosingNow,
  customerId: Option[CustomerId] = None,
  offerAmount: Option[BigInt] = None,
  madeDeclaration: Boolean = false
) extends Submission

object FullDisclosure {
  implicit val format: OFormat[FullDisclosure] = Json.using[Json.WithDefaultValues].format[FullDisclosure]
}
