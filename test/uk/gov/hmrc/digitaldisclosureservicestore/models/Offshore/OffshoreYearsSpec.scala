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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class OffshoreYearsSpec extends AnyFreeSpec with Matchers {

  "OffshoreYears.fromString" - {

    "must return ReasonableExcusePriorTo" in {
      OffshoreYears.fromString("reasonableExcusePriorTo") mustBe Some(ReasonableExcusePriorTo)
    }

    "must return CarelessPriorTo" in {
      OffshoreYears.fromString("carelessPriorTo") mustBe Some(CarelessPriorTo)
    }

    "must return DeliberatePriorTo" in {
      OffshoreYears.fromString("deliberatePriorTo") mustBe Some(DeliberatePriorTo)
    }

    "must return TaxYearStarting for valid year" in {
      OffshoreYears.fromString("2020") mustBe Some(TaxYearStarting(2020))
    }

    "must return None for invalid value" in {
      OffshoreYears.fromString("abc") mustBe None
    }
  }

  "OffshoreYears JSON format" - {

    "must read ReasonableExcusePriorTo" in {
      Json.fromJson[OffshoreYears](JsString("reasonableExcusePriorTo")) mustBe JsSuccess(ReasonableExcusePriorTo)
    }

    "must read CarelessPriorTo" in {
      Json.fromJson[OffshoreYears](JsString("carelessPriorTo")) mustBe JsSuccess(CarelessPriorTo)
    }

    "must read DeliberatePriorTo" in {
      Json.fromJson[OffshoreYears](JsString("deliberatePriorTo")) mustBe JsSuccess(DeliberatePriorTo)
    }

    "must read TaxYearStarting" in {
      Json.fromJson[OffshoreYears](JsString("2024")) mustBe JsSuccess(TaxYearStarting(2024))
    }

    "must write ReasonableExcusePriorTo" in {
      Json.toJson[OffshoreYears](ReasonableExcusePriorTo) mustBe JsString("reasonableExcusePriorTo")
    }

    "must write CarelessPriorTo" in {
      Json.toJson[OffshoreYears](CarelessPriorTo) mustBe JsString("carelessPriorTo")
    }

    "must write DeliberatePriorTo" in {
      Json.toJson[OffshoreYears](DeliberatePriorTo) mustBe JsString("deliberatePriorTo")
    }

    "must write TaxYearStarting as string year" in {
      Json.toJson[OffshoreYears](TaxYearStarting(2024)) mustBe JsString("2024")
    }

    "must return JsError for invalid string" in {
      Json.fromJson[OffshoreYears](JsString("invalid")) mustBe JsError("error.invalid")
    }

    "must return JsError for non-string json" in {
      Json.fromJson[OffshoreYears](JsNumber(2024)) mustBe JsError("error.invalid")
    }
  }
}