/*
 * Copyright 2023 HM Revenue & Customs
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

package crypto

import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec

import models.disclosure._
import models.notification._
import models.store._
import models._
import java.time.{ZoneOffset, LocalDateTime}

class FullDisclosureEncrypterSpec extends AnyFreeSpec with Matchers {

  private val encrypter = new SecureGCMCipherImpl
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val associatedText = "associatedText"
  private val textToEncrypt = "textNotEncrypted"

  val notificationEncrypter = new NotificationEncrypter(encrypter)
  val sut = new FullDisclosureEncrypter(encrypter, notificationEncrypter)

  "FullDisclosureEncrypter" - {

    "must encrypt/decrypt a FullDisclosure" in {
      val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
      val model = FullDisclosure(
        userId = textToEncrypt,
        submissionId = textToEncrypt,
        lastUpdated = instant,
        metadata = Metadata(),
        caseReference = CaseReference(),
        personalDetails = PersonalDetails(Background(), AboutYou()),
        offshoreLiabilities = OffshoreLiabilities(),
        otherLiabilities = OtherLiabilities(),
        reasonForDisclosingNow = ReasonForDisclosingNow(),
        customerId = None
      )   

      val encryptedModel = sut.encryptFullDisclosure(model, associatedText, secretKey)

      encryptedModel.userId mustEqual model.userId
      encryptedModel.submissionId mustEqual model.submissionId
      encryptedModel.lastUpdated mustEqual model.lastUpdated
      encryptedModel.metadata mustEqual model.metadata
      encryptedModel.offshoreLiabilities mustEqual model.offshoreLiabilities
      encryptedModel.otherLiabilities mustEqual model.otherLiabilities

      sut.decryptFullDisclosure(encryptedModel, associatedText, secretKey) mustEqual model
    }

    "must encrypt/decrypt a CaseReference" in {

      val model = CaseReference (
        doYouHaveACaseReference = Some(true),
        whatIsTheCaseReference = Some(textToEncrypt)
      ) 

      val encryptedModel = sut.encryptCaseReference(model, associatedText, secretKey)

      encryptedModel.doYouHaveACaseReference mustEqual model.doYouHaveACaseReference
      encryptedModel.whatIsTheCaseReference.get.value must not equal model.whatIsTheCaseReference.get

      sut.decryptCaseReference(encryptedModel, associatedText, secretKey) mustEqual model
    }

    "must encrypt/decrypt a ReasonForDisclosingNow" in {

      val model = ReasonForDisclosingNow(
        reason = Some(Set(WhyAreYouMakingADisclosure.GovUkGuidance)),
        otherReason = Some(textToEncrypt),
        whyNotBeforeNow = Some(textToEncrypt),
        receivedAdvice = Some(true),
        personWhoGaveAdvice = Some(textToEncrypt),
        adviceOnBehalfOfBusiness = Some(true),
        adviceBusinessName = Some(textToEncrypt),
        personProfession = Some(textToEncrypt),
        adviceGiven = Some(AdviceGiven(textToEncrypt, MonthYear(12, 2012), AdviceContactPreference.Email)),
        whichEmail = Some(WhichEmailAddressCanWeContactYouWith.DifferentEmail),
        whichPhone = Some(WhichTelephoneNumberCanWeContactYouWith.DifferentNumber),
        email = Some(textToEncrypt),
        telephone = Some(textToEncrypt)
      )

      val encryptedModel = sut.encryptReasonForDisclosingNow(model, associatedText, secretKey)

      encryptedModel.reason mustEqual model.reason
      encryptedModel.otherReason mustEqual model.otherReason
      encryptedModel.whyNotBeforeNow mustEqual model.whyNotBeforeNow
      encryptedModel.receivedAdvice mustEqual model.receivedAdvice
      encryptedModel.personWhoGaveAdvice.get.value must not equal model.personWhoGaveAdvice.get
      encryptedModel.adviceOnBehalfOfBusiness mustEqual model.adviceOnBehalfOfBusiness
      encryptedModel.adviceBusinessName.get.value must not equal model.adviceBusinessName.get
      encryptedModel.personProfession mustEqual model.personProfession
      encryptedModel.adviceGiven mustEqual model.adviceGiven
      encryptedModel.whichEmail mustEqual model.whichEmail
      encryptedModel.whichPhone mustEqual model.whichPhone
      encryptedModel.email.get.value must not equal model.email.get
      encryptedModel.telephone.get.value must not equal model.telephone.get

      sut.decryptReasonForDisclosingNow(encryptedModel, associatedText, secretKey) mustEqual model
    }


  }

}
