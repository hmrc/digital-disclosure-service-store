/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class OnshoreYearsSpec extends AnyWordSpec with Matchers {

  "OnshoreYearStarting" should {

    "return startYear as string" in {
      OnshoreYearStarting(2020).toString mustBe "2020"
    }

    "sort years in descending order using compare" in {
      val years = Seq(
        OnshoreYearStarting(2019),
        OnshoreYearStarting(2021),
        OnshoreYearStarting(2020)
      )

      years.sorted mustBe Seq(
        OnshoreYearStarting(2021),
        OnshoreYearStarting(2020),
        OnshoreYearStarting(2019)
      )
    }

    "serialize and deserialize using its own format" in {
      val year = OnshoreYearStarting(2020)

      val json = Json.toJson(year)

      json mustBe Json.obj(
        "startYear" -> 2020
      )

      json.as[OnshoreYearStarting] mustBe year
    }
  }

  "OnshoreYearStarting.findMissingYears" should {

    "return missing years between first and last year" in {
      val years = List(
        OnshoreYearStarting(2020),
        OnshoreYearStarting(2018),
        OnshoreYearStarting(2017)
      )

      OnshoreYearStarting.findMissingYears(years) mustBe List(
        OnshoreYearStarting(2019)
      )
    }

    "return multiple missing years between first and last year in descending order" in {
      val years = List(
        OnshoreYearStarting(2023),
        OnshoreYearStarting(2020),
        OnshoreYearStarting(2018)
      )

      OnshoreYearStarting.findMissingYears(years) mustBe List(
        OnshoreYearStarting(2022),
        OnshoreYearStarting(2021),
        OnshoreYearStarting(2019)
      )
    }

    "return Nil when no year is missing" in {
      val years = List(
        OnshoreYearStarting(2022),
        OnshoreYearStarting(2021),
        OnshoreYearStarting(2020)
      )

      OnshoreYearStarting.findMissingYears(years) mustBe Nil
    }

    "return Nil when list has only one year" in {
      val years = List(
        OnshoreYearStarting(2022)
      )

      OnshoreYearStarting.findMissingYears(years) mustBe Nil
    }

    "return Nil when list is empty" in {
      OnshoreYearStarting.findMissingYears(Nil) mustBe Nil
    }

    "handle unordered input list" in {
      val years = List(
        OnshoreYearStarting(2019),
        OnshoreYearStarting(2022),
        OnshoreYearStarting(2020)
      )

      OnshoreYearStarting.findMissingYears(years) mustBe List(
        OnshoreYearStarting(2021)
      )
    }
  }

  "OnshoreYears.fromString" should {

    "return PriorToThreeYears for priorToThreeYears" in {
      OnshoreYears.fromString("priorToThreeYears") mustBe Some(PriorToThreeYears)
    }

    "return PriorToFiveYears for priorToFiveYears" in {
      OnshoreYears.fromString("priorToFiveYears") mustBe Some(PriorToFiveYears)
    }

    "return PriorToNineteenYears for priorToNineteenYears" in {
      OnshoreYears.fromString("priorToNineteenYears") mustBe Some(PriorToNineteenYears)
    }

    "return OnshoreYearStarting when string is a valid year" in {
      OnshoreYears.fromString("2021") mustBe Some(OnshoreYearStarting(2021))
    }

    "return None for invalid string" in {
      OnshoreYears.fromString("invalid-year") mustBe None
    }

    "return None for empty string" in {
      OnshoreYears.fromString("") mustBe None
    }
  }

  "OnshoreYears Reads" should {

    "read PriorToThreeYears from JsString" in {
      Json.fromJson[OnshoreYears](JsString("priorToThreeYears")) mustBe JsSuccess(PriorToThreeYears)
    }

    "read PriorToFiveYears from JsString" in {
      Json.fromJson[OnshoreYears](JsString("priorToFiveYears")) mustBe JsSuccess(PriorToFiveYears)
    }

    "read PriorToNineteenYears from JsString" in {
      Json.fromJson[OnshoreYears](JsString("priorToNineteenYears")) mustBe JsSuccess(PriorToNineteenYears)
    }

    "read OnshoreYearStarting from numeric string" in {
      Json.fromJson[OnshoreYears](JsString("2022")) mustBe JsSuccess(OnshoreYearStarting(2022))
    }

    "return JsError for invalid string" in {
      Json.fromJson[OnshoreYears](JsString("wrong-value")).isError mustBe true
    }

    "return JsError for JsNumber" in {
      Json.fromJson[OnshoreYears](JsNumber(2022)).isError mustBe true
    }

    "return JsError for JsObject" in {
      Json.fromJson[OnshoreYears](Json.obj("year" -> 2022)).isError mustBe true
    }

    "return JsError for JsNull" in {
      Json.fromJson[OnshoreYears](JsNull).isError mustBe true
    }
  }

  "OnshoreYears Writes" should {

    "write PriorToThreeYears as JsString" in {
      Json.toJson[OnshoreYears](PriorToThreeYears) mustBe JsString("priorToThreeYears")
    }

    "write PriorToFiveYears as JsString" in {
      Json.toJson[OnshoreYears](PriorToFiveYears) mustBe JsString("priorToFiveYears")
    }

    "write PriorToNineteenYears as JsString" in {
      Json.toJson[OnshoreYears](PriorToNineteenYears) mustBe JsString("priorToNineteenYears")
    }

    "write OnshoreYearStarting as JsString containing year" in {
      Json.toJson[OnshoreYears](OnshoreYearStarting(2023)) mustBe JsString("2023")
    }
  }

  "OnshoreYears Format" should {

    "serialize and deserialize PriorToThreeYears" in {
      val json = Json.toJson[OnshoreYears](PriorToThreeYears)

      json mustBe JsString("priorToThreeYears")
      json.as[OnshoreYears] mustBe PriorToThreeYears
    }

    "serialize and deserialize PriorToFiveYears" in {
      val json = Json.toJson[OnshoreYears](PriorToFiveYears)

      json mustBe JsString("priorToFiveYears")
      json.as[OnshoreYears] mustBe PriorToFiveYears
    }

    "serialize and deserialize PriorToNineteenYears" in {
      val json = Json.toJson[OnshoreYears](PriorToNineteenYears)

      json mustBe JsString("priorToNineteenYears")
      json.as[OnshoreYears] mustBe PriorToNineteenYears
    }

    "serialize and deserialize OnshoreYearStarting" in {
      val json = Json.toJson[OnshoreYears](OnshoreYearStarting(2024))

      json mustBe JsString("2024")
      json.as[OnshoreYears] mustBe OnshoreYearStarting(2024)
    }
  }

  "RawOnshoreYears" should {

    "extract year from numeric string" in {
      val result = "2025" match {
        case RawOnshoreYears(year) => Some(year)
        case _                     => None
      }

      result mustBe Some(2025)
    }

    "return None for non numeric string" in {
      val result = "abc" match {
        case RawOnshoreYears(year) => Some(year)
        case _                     => None
      }

      result mustBe None
    }
  }
}
