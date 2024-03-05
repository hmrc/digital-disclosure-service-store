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

package crypto

import config.AppConfig
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import models.notification._
import models.address._
import models.store.Notification
import models.{Metadata, YesNoOrUnsure}

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import models.IncomeOrGainSource
import play.api.Configuration

class NotificationEncrypterSpec extends AnyFreeSpec with Matchers {

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  lazy implicit val appConfig: AppConfig = new AppConfig(Configuration("mongodb.encryption.key" -> secretKey))
  private val encrypter = new SecureGCMCipherImpl
  private val associatedText = "associatedText"
  private val textToEncrypt = "textNotEncrypted"
  private val dateToEncrypt = LocalDate.of(2016,1,10)

  val sut = new NotificationEncrypter(encrypter)

  val addressToEncrypt: Address = Address(
    line1 = textToEncrypt,
    line2 = Some(textToEncrypt),
    line3 = Some(textToEncrypt),
    line4 = Some(textToEncrypt),
    postcode = Some(textToEncrypt),
    country = Country(textToEncrypt)
  )

  "NotificationEncrypter" - {
    "must encrypt/decrypt an Address" in {
      val encryptedAddress = sut.encryptAddress(addressToEncrypt, associatedText)

      encryptedAddress.line1.value must not equal addressToEncrypt.line1
      encryptedAddress.line2.get.value must not equal addressToEncrypt.line2.get
      encryptedAddress.line3.get.value must not equal addressToEncrypt.line3.get
      encryptedAddress.line4.get.value must not equal addressToEncrypt.line4.get
      encryptedAddress.postcode.get.value must not equal addressToEncrypt.postcode.get
      encryptedAddress.country.value must not equal addressToEncrypt.country.code

      sut.decryptAddress(encryptedAddress, associatedText) mustEqual addressToEncrypt
    }
  
    "must encrypt/decrypt an AboutTheEstate" in {

      val aboutTheEstate = AboutTheEstate (
        fullName = Some(textToEncrypt),
        dateOfBirth = Some(dateToEncrypt),
        mainOccupation = Some(textToEncrypt),
        doTheyHaveANino = Some(YesNoOrUnsure.Yes),
        nino = Some(textToEncrypt),
        registeredForVAT = Some(YesNoOrUnsure.Yes),
        vatRegNumber = Some(textToEncrypt),
        registeredForSA = Some(YesNoOrUnsure.Yes),
        sautr = Some(textToEncrypt),
        address = Some(addressToEncrypt)
      )   

      val encryptedAboutTheEstate = sut.encryptAboutTheEstate(aboutTheEstate, associatedText)

      encryptedAboutTheEstate.fullName.get.value must not equal aboutTheEstate.fullName.get
      encryptedAboutTheEstate.dateOfBirth.get.value must not equal aboutTheEstate.dateOfBirth.get.toString
      encryptedAboutTheEstate.mainOccupation.get mustEqual aboutTheEstate.mainOccupation.get
      encryptedAboutTheEstate.doTheyHaveANino.get mustEqual aboutTheEstate.doTheyHaveANino.get
      encryptedAboutTheEstate.nino.get.value must not equal aboutTheEstate.nino.get
      encryptedAboutTheEstate.registeredForVAT.get mustEqual aboutTheEstate.registeredForVAT.get
      encryptedAboutTheEstate.vatRegNumber.get.value must not equal aboutTheEstate.vatRegNumber.get
      encryptedAboutTheEstate.registeredForSA.get mustEqual aboutTheEstate.registeredForSA.get
      encryptedAboutTheEstate.sautr.get.value must not equal aboutTheEstate.sautr.get

      sut.decryptAboutTheEstate(encryptedAboutTheEstate, associatedText) mustEqual aboutTheEstate
    }

    "must encrypt/decrypt an AboutTheLLP" in {

      val model = AboutTheLLP (
        name = Some(textToEncrypt),
        address = Some(addressToEncrypt)
      ) 

      val encryptedModel = sut.encryptAboutTheLLP(model, associatedText)

      encryptedModel.name.get.value must not equal model.name.get
      sut.decryptAboutTheLLP(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt an AboutTheTrust" in {

      val model = AboutTheTrust (
        name = Some(textToEncrypt),
        address = Some(addressToEncrypt)
      ) 

      val encryptedModel = sut.encryptAboutTheTrust(model, associatedText)

      encryptedModel.name.get.value must not equal model.name.get
      sut.decryptAboutTheTrust(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt an AboutTheCompany" in {

      val model = AboutTheCompany (
        name = Some(textToEncrypt),
        registrationNumber = Some(textToEncrypt),
        address = Some(addressToEncrypt)
      ) 

      val encryptedModel = sut.encryptAboutTheCompany(model, associatedText)

      encryptedModel.name.get.value must not equal model.name.get
      encryptedModel.registrationNumber.get.value must not equal model.registrationNumber.get
      sut.decryptAboutTheCompany(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt an AboutTheIndividual" in {

      val model = AboutTheIndividual (
        fullName = Some(textToEncrypt),
        dateOfBirth = Some(dateToEncrypt),
        mainOccupation = Some(textToEncrypt),
        doTheyHaveANino = Some(YesNoOrUnsure.Yes),
        nino = Some(textToEncrypt),
        registeredForVAT = Some(YesNoOrUnsure.Yes),
        vatRegNumber = Some(textToEncrypt),
        registeredForSA = Some(YesNoOrUnsure.Yes),
        sautr = Some(textToEncrypt),
        address = Some(addressToEncrypt)
      )   

      val encryptedModel = sut.encryptAboutTheIndividual(model, associatedText)

      encryptedModel.fullName.get.value must not equal model.fullName.get
      encryptedModel.dateOfBirth.get.value must not equal model.dateOfBirth.get.toString
      encryptedModel.mainOccupation.get mustEqual model.mainOccupation.get
      encryptedModel.doTheyHaveANino.get mustEqual model.doTheyHaveANino.get
      encryptedModel.nino.get.value must not equal model.nino.get
      encryptedModel.registeredForVAT.get mustEqual model.registeredForVAT.get
      encryptedModel.vatRegNumber.get.value must not equal model.vatRegNumber.get
      encryptedModel.registeredForSA.get mustEqual model.registeredForSA.get
      encryptedModel.sautr.get.value must not equal model.sautr.get

      sut.decryptAboutTheIndividual(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt an AboutYou" in {

      val model = AboutYou (
        fullName = Some(textToEncrypt),
        telephoneNumber = Some(textToEncrypt),
        contactPreference = Some(ContactPreferences(Set(Email,Telephone))),
        emailAddress = Some(textToEncrypt),
        dateOfBirth = Some(dateToEncrypt),
        mainOccupation = Some(textToEncrypt),
        doYouHaveANino = Some(YesNoOrUnsure.Yes),
        nino = Some(textToEncrypt),
        registeredForVAT = Some(YesNoOrUnsure.Yes),
        vatRegNumber = Some(textToEncrypt),
        registeredForSA = Some(YesNoOrUnsure.Yes),
        sautr = Some(textToEncrypt),
        address = Some(addressToEncrypt)
      )   

      val encryptedModel = sut.encryptAboutYou(model, associatedText)

      encryptedModel.fullName.get.value must not equal model.fullName.get
      encryptedModel.telephoneNumber.get.value must not equal model.telephoneNumber.get
      encryptedModel.contactPreference.get mustEqual model.contactPreference.get
      encryptedModel.emailAddress.get.value must not equal model.emailAddress.get
      encryptedModel.dateOfBirth.get.value must not equal model.dateOfBirth.get.toString
      encryptedModel.mainOccupation.get mustEqual model.mainOccupation.get
      encryptedModel.doYouHaveANino.get mustEqual model.doYouHaveANino.get
      encryptedModel.nino.get.value must not equal model.nino.get
      encryptedModel.registeredForVAT.get mustEqual model.registeredForVAT.get
      encryptedModel.vatRegNumber.get.value must not equal model.vatRegNumber.get
      encryptedModel.registeredForSA.get mustEqual model.registeredForSA.get
      encryptedModel.sautr.get.value must not equal model.sautr.get

      sut.decryptAboutYou(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt a Background" in {

      val model = Background (
        haveYouReceivedALetter = Some(true),
        letterReferenceNumber = Some(textToEncrypt),
        disclosureEntity = None,
        areYouRepresetingAnOrganisation = Some(true),
        organisationName = Some(textToEncrypt),
        offshoreLiabilities = Some(true),
        onshoreLiabilities = Some(true),
        incomeSource = Some(Set(IncomeOrGainSource.Dividends)),
        otherIncomeSource = Some("Some source")
      )   

      val encryptedModel = sut.encryptBackground(model, associatedText)

      encryptedModel.haveYouReceivedALetter mustEqual model.haveYouReceivedALetter
      encryptedModel.letterReferenceNumber mustEqual model.letterReferenceNumber
      encryptedModel.disclosureEntity mustEqual model.disclosureEntity
      encryptedModel.areYouRepresetingAnOrganisation mustEqual model.areYouRepresetingAnOrganisation
      encryptedModel.offshoreLiabilities mustEqual model.offshoreLiabilities
      encryptedModel.onshoreLiabilities mustEqual model.onshoreLiabilities
      encryptedModel.offshoreLiabilities mustEqual model.offshoreLiabilities
      encryptedModel.onshoreLiabilities mustEqual model.onshoreLiabilities

      encryptedModel.organisationName.get.value must not equal model.organisationName.get

      sut.decryptBackground(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt a PersonalDetails" in {

      val model = PersonalDetails (
        background = Background(),
        aboutYou = AboutYou(),
        aboutTheIndividual = Some(AboutTheIndividual()),
        aboutTheCompany = Some(AboutTheCompany()),
        aboutTheTrust = Some(AboutTheTrust()),
        aboutTheLLP = Some(AboutTheLLP()),
        aboutTheEstate = Some(AboutTheEstate())
      )   

      val encryptedModel = sut.encryptPersonalDetails(model, associatedText)
      sut.decryptPersonalDetails(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt a Notification" in {
      val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
      val model = Notification (
        userId = textToEncrypt,
        submissionId = textToEncrypt,
        lastUpdated = instant,
        created = instant,
        metadata = Metadata(),
        personalDetails = PersonalDetails(Background(), AboutYou()),
        customerId = None
      )   

      val encryptedModel = sut.encryptNotification(model, associatedText)

      encryptedModel.userId mustEqual model.userId
      encryptedModel.submissionId mustEqual model.submissionId
      encryptedModel.lastUpdated mustEqual model.lastUpdated
      encryptedModel.metadata mustEqual model.metadata

      sut.decryptNotification(encryptedModel, associatedText) mustEqual model
    }


  }

}
