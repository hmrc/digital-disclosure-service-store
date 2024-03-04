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

import models._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json, JsObject}
import models.notification._
import java.time.LocalDateTime
import java.time.ZoneOffset

class SubmissionSpec extends AnyWordSpec with Matchers  {

  "reads" should {
    "convert json to Notification where created is missing" in {
      val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
      val testNotification: Submission = Notification("123", "123", instant, instant, Metadata(), PersonalDetails(Background(), AboutYou()))
      val actual = Json.toJson(testNotification).as[JsObject]  - "created"
      actual.validate[Notification] shouldBe a[JsSuccess[_]]
    }

    "convert json to Notification where declaration is missing" in {
      val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
      val testNotification: Submission = Notification("123", "123", instant, instant, Metadata(), PersonalDetails(Background(), AboutYou()))
      val actual = Json.toJson(testNotification).as[JsObject]  - "madeDeclaration"
      actual.validate[Notification] shouldBe a[JsSuccess[_]]
    }
  }
  
}
