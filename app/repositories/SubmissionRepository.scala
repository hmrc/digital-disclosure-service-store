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

package repositories

import config.AppConfig
import models._
import models.store._
import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import com.google.inject.{ImplementedBy, Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import crypto.SubmissionEncrypter

@Singleton
class SubmissionRepositoryImpl @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig,
  clock: Clock,
  encrypter: SubmissionEncrypter
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[EncryptedSubmission](
      collectionName = "digital-disclosure",
      mongoComponent = mongoComponent,
      domainFormat = EncryptedSubmission.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.DAYS)
        ),
        IndexModel(
          Indexes.ascending("userId"),
          IndexOptions()
            .name("userIdIdx")
        ),
        IndexModel(
          Indexes.compoundIndex(
            Indexes.ascending("userId"),
            Indexes.ascending("submissionId")
          ),
          IndexOptions()
            .name("idsIdx")
            .unique(true)
        )
      ),
      replaceIndexes = true,
      extraCodecs = Codecs.playFormatSumCodecs(EncryptedSubmission.format)
    )
    with SubmissionRepository {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  def get(userId: String, submissionId: String): Future[Option[Submission]] =
    collection
      .find(
        Filters.and(
          Filters.equal("userId", userId),
          Filters.equal("submissionId", submissionId)
        )
      )
      .map(encrypter.decryptSubmission(_, userId))
      .headOption()

  def get(userId: String): Future[Seq[Submission]] =
    collection
      .find(
        Filters.and(
          Filters.equal("userId", userId)
        )
      )
      .map(encrypter.decryptSubmission(_, userId))
      .toFuture()

  def set(submission: Submission): Future[Boolean] = {

    val updatedSubmission = submission match {
      case notification: Notification => notification.copy(lastUpdated = clock.instant())
      case disclosure: FullDisclosure => disclosure.copy(lastUpdated = clock.instant())
    }

    collection
      .replaceOne(
        filter = Filters.and(
          Filters.equal("userId", submission.userId),
          Filters.equal("submissionId", submission.submissionId)
        ),
        replacement = encrypter.encryptSubmission(updatedSubmission, submission.userId),
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(userId: String, submissionId: String): Future[Boolean] =
    collection
      .findOneAndDelete(
        Filters.and(
          Filters.equal("userId", userId),
          Filters.equal("submissionId", submissionId)
        )
      )
      .toFuture()
      .map(_ => true)

}

@ImplementedBy(classOf[SubmissionRepositoryImpl])
trait SubmissionRepository {
  def set(submission: Submission): Future[Boolean]
  def get(userId: String, submissionId: String): Future[Option[Submission]]
  def get(userId: String): Future[Seq[Submission]]
  def clear(userId: String, submissionId: String): Future[Boolean]
}
