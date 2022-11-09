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

package repositories

import config.AppConfig
import models.notification.Notification
import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import com.google.inject.{Inject, ImplementedBy, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationRepositoryImpl @Inject()(
                                   mongoComponent: MongoComponent,
                                   appConfig: AppConfig,
                                   clock: Clock
                                 )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[Notification] (
    collectionName = "notification-of-intent",
    mongoComponent = mongoComponent,
    domainFormat   = Notification.format,
    indexes        = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(90, TimeUnit.DAYS)
      ),
      IndexModel(
        Indexes.ascending("userId"),
        IndexOptions()
          .name("userIdIdx")
      ),
      IndexModel(
        Indexes.compoundIndex(
          Indexes.ascending("userId"),
          Indexes.ascending("notificationId")
        ),
        IndexOptions()
          .name("idsIdx")
          .unique(true)
      ),
    ),
    replaceIndexes = true
  ) with NotificationRepository {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  def get(userId: String, notificationId: String): Future[Option[Notification]] =
    collection
      .find(Filters.and(
        Filters.equal("userId", userId),
        Filters.equal("notificationId", notificationId)
      ))
      .headOption()
        
  def get(userId: String): Future[Seq[Notification]] =
    collection
      .find(Filters.and(
        Filters.equal("userId", userId)
      ))
      .toFuture()

  def set(notification: Notification): Future[Boolean] = {
    collection
      .replaceOne(
        filter      = Filters.and(
          Filters.equal("userId", notification.userId),
          Filters.equal("notificationId", notification.notificationId)
        ),
        replacement = notification.copy(lastUpdated = clock.instant()),
        options     = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(userId: String, notificationId: String): Future[Boolean] =
    collection.findOneAndDelete(Filters.and(
      Filters.equal("userId", userId),
      Filters.equal("notificationId", notificationId)
    )).toFuture().map(_ => true)
}

@ImplementedBy(classOf[NotificationRepositoryImpl])
trait NotificationRepository {
  def set(notification: Notification): Future[Boolean]
  def get(userId: String, notificationId: String): Future[Option[Notification]]
  def get(userId: String): Future[Seq[Notification]]
  def clear(userId: String, notificationId: String): Future[Boolean]
} 