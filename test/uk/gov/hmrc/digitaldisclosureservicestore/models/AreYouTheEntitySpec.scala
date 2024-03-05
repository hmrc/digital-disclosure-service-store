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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, JsSuccess, Json}

class AreYouTheEntitySpec extends AnyWordSpec with Matchers  {

  "reads" should {
    "convert JsValue true to YesIAm" in {
      val actual = Json.toJson(true)
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.YesIAm)
    }

    "convert JsString false to IAmAnAccountantOrTaxAgent" in {
      val actual = Json.toJson(false)
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.IAmAnAccountantOrTaxAgent)
    }

    "convert JsString yes to YesIAm" in {
      val actual = JsString("yes")
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.YesIAm)
    }

    "convert JsString accountant to IAmAnAccountantOrTaxAgent" in {
      val actual = JsString("accountant")
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.IAmAnAccountantOrTaxAgent)
    }

    "convert JsString friend to IAmAFriend" in {
      val actual = JsString("friend")
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.IAmAFriend)
    }

    "convert JsString voluntaryOrganisation to VoluntaryOrganisation" in {
      val actual = JsString("voluntaryOrganisation")
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.VoluntaryOrganisation)
    }

    "convert JsString powerOfAttorney to PowerOfAttorney" in {
      val actual = JsString("powerOfAttorney")
      actual.validate[AreYouTheEntity] shouldEqual JsSuccess(AreYouTheEntity.PowerOfAttorney)
    }

  }

  "writes" should {
    "convert YesIAm to JsString yes" in {
      val actual: AreYouTheEntity = AreYouTheEntity.YesIAm
      Json.toJson(actual) shouldEqual JsString("yes")
    }

    "convert IAmAnAccountantOrTaxAgent to JsString accountant" in {
      val actual: AreYouTheEntity = AreYouTheEntity.IAmAnAccountantOrTaxAgent
      Json.toJson(actual) shouldEqual JsString("accountant")
    }

  }
  
}
