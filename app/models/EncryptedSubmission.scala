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

package models

import java.time.Instant
import models.notification._
import models.disclosure._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

sealed trait EncryptedSubmission {
  def userId: String
  def submissionId: String
  def lastUpdated: Instant
}

object EncryptedSubmission {
  implicit val format: OFormat[EncryptedSubmission] = Json.using[Json.WithDefaultValues].format[EncryptedSubmission]
}

final case class EncryptedNotification(
  userId: String,
  submissionId: String,
  lastUpdated: Instant,
  created: Instant = Instant.now(),
  metadata: Metadata,
  personalDetails: EncryptedPersonalDetails,
  customerId: Option[CustomerId] = None,
  madeDeclaration: Boolean = false
) extends EncryptedSubmission

object EncryptedNotification {

  val reads: Reads[EncryptedNotification] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "userId").read[String] and
        (__ \ "submissionId").read[String] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat) and
        ((__ \ "created").read(MongoJavatimeFormats.instantFormat) or Reads.pure(Instant.now())) and
        (__ \ "metadata").read[Metadata] and
        (__ \ "personalDetails").read[EncryptedPersonalDetails] and
        (__ \ "customerId").readNullable[CustomerId] and
        ((__ \ "madeDeclaration").read[Boolean] or Reads.pure(false))
    )(EncryptedNotification.apply _)
  }

  val writes: OWrites[EncryptedNotification] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "userId").write[String] and
        (__ \ "submissionId").write[String] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "created").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "metadata").write[Metadata] and
        (__ \ "personalDetails").write[EncryptedPersonalDetails] and
        (__ \ "customerId").writeNullable[CustomerId] and
        (__ \ "madeDeclaration").write[Boolean]
    )(unlift(EncryptedNotification.unapply))
  }

  implicit val format: OFormat[EncryptedNotification] = OFormat(reads, writes)
}

final case class EncryptedFullDisclosure(
  userId: String,
  submissionId: String,
  lastUpdated: Instant,
  created: Instant = Instant.now(),
  metadata: Metadata,
  caseReference: EncryptedCaseReference,
  personalDetails: EncryptedPersonalDetails,
  onshoreLiabilities: Option[OnshoreLiabilities] = None,
  offshoreLiabilities: OffshoreLiabilities,
  otherLiabilities: OtherLiabilities,
  reasonForDisclosingNow: EncryptedReasonForDisclosingNow,
  customerId: Option[CustomerId] = None,
  madeDeclaration: Boolean = false
) extends EncryptedSubmission

object EncryptedFullDisclosure {

  val reads: Reads[EncryptedFullDisclosure] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "userId").read[String] and
        (__ \ "submissionId").read[String] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat) and
        ((__ \ "created").read(MongoJavatimeFormats.instantFormat) or Reads.pure(Instant.now())) and
        (__ \ "metadata").read[Metadata] and
        (__ \ "caseReference").read[EncryptedCaseReference] and
        (__ \ "personalDetails").read[EncryptedPersonalDetails] and
        (__ \ "onshoreLiabilities").readNullable[OnshoreLiabilities] and
        (__ \ "offshoreLiabilities").read[OffshoreLiabilities] and
        (__ \ "otherLiabilities").read[OtherLiabilities] and
        (__ \ "reasonForDisclosingNow").read[EncryptedReasonForDisclosingNow] and
        (__ \ "customerId").readNullable[CustomerId] and
        ((__ \ "madeDeclaration").read[Boolean] or Reads.pure(false))
    )(EncryptedFullDisclosure.apply _)
  }

  val writes: OWrites[EncryptedFullDisclosure] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "userId").write[String] and
        (__ \ "submissionId").write[String] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "created").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "metadata").write[Metadata] and
        (__ \ "caseReference").write[EncryptedCaseReference] and
        (__ \ "personalDetails").write[EncryptedPersonalDetails] and
        (__ \ "onshoreLiabilities").writeNullable[OnshoreLiabilities] and
        (__ \ "offshoreLiabilities").write[OffshoreLiabilities] and
        (__ \ "otherLiabilities").write[OtherLiabilities] and
        (__ \ "reasonForDisclosingNow").write[EncryptedReasonForDisclosingNow] and
        (__ \ "customerId").writeNullable[CustomerId] and
        (__ \ "madeDeclaration").write[Boolean]
    )(unlift(EncryptedFullDisclosure.unapply))
  }

  implicit val format: OFormat[EncryptedFullDisclosure] = OFormat(reads, writes)
}
