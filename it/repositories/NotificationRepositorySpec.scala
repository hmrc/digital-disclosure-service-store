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

import models.notification._
import util.MutableClock
import config.AppConfig
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.Configuration
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant
import crypto._

class NotificationRepositorySpec extends AnyFreeSpec
  with Matchers with OptionValues
  with DefaultPlayMongoRepositorySupport[EncryptedNotification]
  with ScalaFutures with IntegrationPatience
  with BeforeAndAfterEach {

  private val now: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)
  private val clock: MutableClock = MutableClock(now)
  private val encrypter = new NotificationEncrypter(new SecureGCMCipherImpl)

  override def beforeEach(): Unit = {
    super.beforeEach()
    clock.set(now)
  }

  override protected def repository = new NotificationRepositoryImpl(
    mongoComponent = mongoComponent,
    appConfig = new AppConfig(Configuration("appName" -> "test app", "lock-ttl" -> 30, "mongodb.encryption.key" -> "key")),
    clock = clock,
    encrypter = encrypter
  )

  private val testNotification = Notification("user", "id", now, Metadata(), Background(), AboutYou())

  "set" - {

    "must insert if no record exists" in {
      repository.set(testNotification).futureValue mustEqual true
      repository.get("user", "id").futureValue.value mustEqual testNotification
    }


    "must update a record if it exists and return it" in {
      val expected = testNotification.copy(metadata = Metadata(reference = Some("12345")), lastUpdated = clock.instant())
      repository.set(testNotification).futureValue
      repository.set(expected).futureValue mustEqual true
      repository.get("user", "id").futureValue.value mustEqual expected
    }

}

  "get by userId and id" - {

    "must return an item that matches the userId and id" in {
      repository.set(testNotification).futureValue
      repository.set(testNotification.copy(userId = "user2", notificationId = "id")).futureValue
      repository.get("user", "id").futureValue.value mustEqual testNotification
      repository.get("user2", "id").futureValue.value mustEqual testNotification.copy(userId = "user2", notificationId = "id")
    }

    "must return `None` when there is no item matching the userId and id" in {
      repository.set(testNotification).futureValue
      repository.get("user", "id2").futureValue mustNot be (defined)
    }
  }

  "get by userId" - {

    "must return all items that match the userId" in {
      repository.set(testNotification).futureValue
      repository.set(testNotification.copy(userId = "user", notificationId = "id2")).futureValue
      repository.get("user").futureValue mustEqual Seq(testNotification, testNotification.copy(userId = "user", notificationId = "id2"))
    }

    "must return `Nil` when there is no item matching the userId and id" in {
      repository.set(testNotification).futureValue
      repository.get("user2").futureValue mustEqual Nil
    }
  }

  "clear" - {

    "must remove an item if it matches the id and owner" in {
      repository.set(testNotification).futureValue
      repository.set(testNotification.copy(userId = "user", notificationId = "id2")).futureValue
      repository.set(testNotification.copy(userId = "user2", notificationId = "id")).futureValue
      repository.clear("user", "id").futureValue
      repository.get("user", "id").futureValue mustNot be (defined)
      repository.get("user", "id2").futureValue mustBe defined
      repository.get("user2", "id").futureValue mustBe defined
    }

    "must fail silently when trying to remove something that doesn't exist" in {
      repository.set(testNotification.copy(userId = "user", notificationId = "id2")).futureValue
      repository.set(testNotification.copy(userId = "user2", notificationId = "id")).futureValue
      repository.clear("user", "id").futureValue
      repository.get("user", "id").futureValue mustNot be (defined)
      repository.get("user", "id2").futureValue mustBe defined
      repository.get("user2", "id").futureValue mustBe defined
    }
  }

}