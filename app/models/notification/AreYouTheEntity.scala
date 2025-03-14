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

import play.api.libs.json._

sealed trait AreYouTheEntity

object AreYouTheEntity {

  case object YesIAm extends WithName("yes") with AreYouTheEntity
  case object IAmAnAccountantOrTaxAgent extends WithName("accountant") with AreYouTheEntity
  case object IAmAFriend extends WithName("friend") with AreYouTheEntity
  case object VoluntaryOrganisation extends WithName("voluntaryOrganisation") with AreYouTheEntity
  case object PowerOfAttorney extends WithName("powerOfAttorney") with AreYouTheEntity

  implicit val reads: Reads[AreYouTheEntity] = Reads {
    case JsString("yes")                   => JsSuccess(YesIAm)
    case JsString("accountant")            => JsSuccess(IAmAnAccountantOrTaxAgent)
    case JsString("friend")                => JsSuccess(IAmAFriend)
    case JsString("voluntaryOrganisation") => JsSuccess(VoluntaryOrganisation)
    case JsString("powerOfAttorney")       => JsSuccess(PowerOfAttorney)
    case value: JsValue                    =>
      value.validate[Boolean] match {
        case JsSuccess(true, _)  => JsSuccess(YesIAm)
        case JsSuccess(false, _) => JsSuccess(IAmAnAccountantOrTaxAgent)
        case _                   => JsError("error.invalid")
      }
    case _                                 => JsError("error.invalid")
  }

  implicit val writes: Writes[AreYouTheEntity] = Writes[AreYouTheEntity](value => JsString(value.toString))

  implicit val format: Format[AreYouTheEntity] = Format(reads, writes)

}
