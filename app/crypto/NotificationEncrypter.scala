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

import com.google.inject.{Inject, Singleton}
import models.store.Notification
import models.EncryptedNotification
import models.notification._
import models.address._
import java.time.LocalDate

@Singleton
class NotificationEncrypter @Inject() (crypto: SecureGCMCipher) {

  def encryptNotification(notification: Notification, sessionId: String): EncryptedNotification =
    EncryptedNotification(
      userId = notification.userId,
      submissionId = notification.submissionId,
      lastUpdated = notification.lastUpdated,
      created = notification.created,
      metadata = notification.metadata,
      personalDetails = encryptPersonalDetails(notification.personalDetails, sessionId),
      customerId = notification.customerId,
      madeDeclaration = notification.madeDeclaration
    )

  def decryptNotification(notification: EncryptedNotification, sessionId: String): Notification =
    Notification(
      userId = notification.userId,
      submissionId = notification.submissionId,
      lastUpdated = notification.lastUpdated,
      created = notification.created,
      metadata = notification.metadata,
      personalDetails = decryptPersonalDetails(notification.personalDetails, sessionId),
      customerId = notification.customerId,
      madeDeclaration = notification.madeDeclaration
    )

  def encryptPersonalDetails(notification: PersonalDetails, sessionId: String): EncryptedPersonalDetails =
    EncryptedPersonalDetails(
      background = encryptBackground(notification.background, sessionId),
      aboutYou = encryptAboutYou(notification.aboutYou, sessionId),
      aboutTheIndividual = notification.aboutTheIndividual.map(encryptAboutTheIndividual(_, sessionId)),
      aboutTheCompany = notification.aboutTheCompany.map(encryptAboutTheCompany(_, sessionId)),
      aboutTheTrust = notification.aboutTheTrust.map(encryptAboutTheTrust(_, sessionId)),
      aboutTheLLP = notification.aboutTheLLP.map(encryptAboutTheLLP(_, sessionId)),
      aboutTheEstate = notification.aboutTheEstate.map(encryptAboutTheEstate(_, sessionId))
    )

  def decryptPersonalDetails(notification: EncryptedPersonalDetails, sessionId: String): PersonalDetails =
    PersonalDetails(
      background = decryptBackground(notification.background, sessionId),
      aboutYou = decryptAboutYou(notification.aboutYou, sessionId),
      aboutTheIndividual = notification.aboutTheIndividual.map(decryptAboutTheIndividual(_, sessionId)),
      aboutTheCompany = notification.aboutTheCompany.map(decryptAboutTheCompany(_, sessionId)),
      aboutTheTrust = notification.aboutTheTrust.map(decryptAboutTheTrust(_, sessionId)),
      aboutTheLLP = notification.aboutTheLLP.map(decryptAboutTheLLP(_, sessionId)),
      aboutTheEstate = notification.aboutTheEstate.map(decryptAboutTheEstate(_, sessionId))
    )

  def encryptBackground(background: Background, sessionId: String): EncryptedBackground = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedBackground(
      haveYouReceivedALetter = background.haveYouReceivedALetter,
      letterReferenceNumber = background.letterReferenceNumber,
      disclosureEntity = background.disclosureEntity,
      areYouRepresetingAnOrganisation = background.areYouRepresetingAnOrganisation,
      organisationName = background.organisationName.map(e),
      offshoreLiabilities = background.offshoreLiabilities,
      onshoreLiabilities = background.onshoreLiabilities,
      incomeSource = background.incomeSource,
      otherIncomeSource = background.otherIncomeSource
    )
  }

  def decryptBackground(background: EncryptedBackground, sessionId: String): Background = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    Background(
      haveYouReceivedALetter = background.haveYouReceivedALetter,
      letterReferenceNumber = background.letterReferenceNumber,
      disclosureEntity = background.disclosureEntity,
      areYouRepresetingAnOrganisation = background.areYouRepresetingAnOrganisation,
      organisationName = background.organisationName.map(d),
      offshoreLiabilities = background.offshoreLiabilities,
      onshoreLiabilities = background.onshoreLiabilities,
      incomeSource = background.incomeSource,
      otherIncomeSource = background.otherIncomeSource
    )
  }

  def encryptAddress(address: Address, sessionId: String): EncryptedAddress = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAddress(
      line1 = e(address.line1),
      line2 = address.line2.map(e),
      line3 = address.line3.map(e),
      line4 = address.line4.map(e),
      postcode = address.postcode.map(e),
      country = e(address.country.code)
    )
  }

  def decryptAddress(address: EncryptedAddress, sessionId: String): Address = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    Address(
      line1 = d(address.line1),
      line2 = address.line2.map(d),
      line3 = address.line3.map(d),
      line4 = address.line4.map(d),
      postcode = address.postcode.map(d),
      country = Country(d(address.country))
    )
  }

  def encryptAboutYou(aboutYou: AboutYou, sessionId: String): EncryptedAboutYou = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutYou(
      fullName = aboutYou.fullName.map(e),
      telephoneNumber = aboutYou.telephoneNumber.map(e),
      contactPreference = aboutYou.contactPreference,
      emailAddress = aboutYou.emailAddress.map(e),
      dateOfBirth = aboutYou.dateOfBirth.map(dob => e(dob.toString)),
      mainOccupation = aboutYou.mainOccupation,
      doYouHaveANino = aboutYou.doYouHaveANino,
      nino = aboutYou.nino.map(e),
      registeredForVAT = aboutYou.registeredForVAT,
      vatRegNumber = aboutYou.vatRegNumber.map(e),
      registeredForSA = aboutYou.registeredForSA,
      sautr = aboutYou.sautr.map(e),
      address = aboutYou.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutYou(aboutYou: EncryptedAboutYou, sessionId: String): AboutYou = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutYou(
      fullName = aboutYou.fullName.map(d),
      telephoneNumber = aboutYou.telephoneNumber.map(d),
      contactPreference = aboutYou.contactPreference,
      emailAddress = aboutYou.emailAddress.map(d),
      dateOfBirth = aboutYou.dateOfBirth.map(dob => LocalDate.parse(d(dob))),
      mainOccupation = aboutYou.mainOccupation,
      doYouHaveANino = aboutYou.doYouHaveANino,
      nino = aboutYou.nino.map(d),
      registeredForVAT = aboutYou.registeredForVAT,
      vatRegNumber = aboutYou.vatRegNumber.map(d),
      registeredForSA = aboutYou.registeredForSA,
      sautr = aboutYou.sautr.map(d),
      address = aboutYou.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheIndividual(
    aboutTheIndividual: AboutTheIndividual,
    sessionId: String
  ): EncryptedAboutTheIndividual = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheIndividual(
      fullName = aboutTheIndividual.fullName.map(e),
      dateOfBirth = aboutTheIndividual.dateOfBirth.map(dob => e(dob.toString)),
      mainOccupation = aboutTheIndividual.mainOccupation,
      doTheyHaveANino = aboutTheIndividual.doTheyHaveANino,
      nino = aboutTheIndividual.nino.map(e),
      registeredForVAT = aboutTheIndividual.registeredForVAT,
      vatRegNumber = aboutTheIndividual.vatRegNumber.map(e),
      registeredForSA = aboutTheIndividual.registeredForSA,
      sautr = aboutTheIndividual.sautr.map(e),
      address = aboutTheIndividual.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheIndividual(
    aboutTheIndividual: EncryptedAboutTheIndividual,
    sessionId: String
  ): AboutTheIndividual = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheIndividual(
      fullName = aboutTheIndividual.fullName.map(d),
      dateOfBirth = aboutTheIndividual.dateOfBirth.map(dob => LocalDate.parse(d(dob))),
      mainOccupation = aboutTheIndividual.mainOccupation,
      doTheyHaveANino = aboutTheIndividual.doTheyHaveANino,
      nino = aboutTheIndividual.nino.map(d),
      registeredForVAT = aboutTheIndividual.registeredForVAT,
      vatRegNumber = aboutTheIndividual.vatRegNumber.map(d),
      registeredForSA = aboutTheIndividual.registeredForSA,
      sautr = aboutTheIndividual.sautr.map(d),
      address = aboutTheIndividual.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheCompany(aboutTheCompany: AboutTheCompany, sessionId: String): EncryptedAboutTheCompany = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheCompany(
      name = aboutTheCompany.name.map(e),
      registrationNumber = aboutTheCompany.registrationNumber.map(e),
      address = aboutTheCompany.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheCompany(aboutTheCompany: EncryptedAboutTheCompany, sessionId: String): AboutTheCompany = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheCompany(
      name = aboutTheCompany.name.map(d),
      registrationNumber = aboutTheCompany.registrationNumber.map(d),
      address = aboutTheCompany.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheTrust(aboutTheTrust: AboutTheTrust, sessionId: String): EncryptedAboutTheTrust = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheTrust(
      name = aboutTheTrust.name.map(e),
      address = aboutTheTrust.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheTrust(aboutTheTrust: EncryptedAboutTheTrust, sessionId: String): AboutTheTrust = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheTrust(
      name = aboutTheTrust.name.map(d),
      address = aboutTheTrust.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheLLP(aboutTheLLP: AboutTheLLP, sessionId: String): EncryptedAboutTheLLP = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheLLP(
      name = aboutTheLLP.name.map(e),
      address = aboutTheLLP.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheLLP(aboutTheLLP: EncryptedAboutTheLLP, sessionId: String): AboutTheLLP = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheLLP(
      name = aboutTheLLP.name.map(d),
      address = aboutTheLLP.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheEstate(aboutTheEstate: AboutTheEstate, sessionId: String): EncryptedAboutTheEstate = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheEstate(
      fullName = aboutTheEstate.fullName.map(e),
      dateOfBirth = aboutTheEstate.dateOfBirth.map(dob => e(dob.toString)),
      mainOccupation = aboutTheEstate.mainOccupation,
      doTheyHaveANino = aboutTheEstate.doTheyHaveANino,
      nino = aboutTheEstate.nino.map(e),
      registeredForVAT = aboutTheEstate.registeredForVAT,
      vatRegNumber = aboutTheEstate.vatRegNumber.map(e),
      registeredForSA = aboutTheEstate.registeredForSA,
      sautr = aboutTheEstate.sautr.map(e),
      address = aboutTheEstate.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheEstate(aboutTheEstate: EncryptedAboutTheEstate, sessionId: String): AboutTheEstate = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheEstate(
      fullName = aboutTheEstate.fullName.map(d),
      dateOfBirth = aboutTheEstate.dateOfBirth.map(dob => LocalDate.parse(d(dob))),
      mainOccupation = aboutTheEstate.mainOccupation,
      doTheyHaveANino = aboutTheEstate.doTheyHaveANino,
      nino = aboutTheEstate.nino.map(d),
      registeredForVAT = aboutTheEstate.registeredForVAT,
      vatRegNumber = aboutTheEstate.vatRegNumber.map(d),
      registeredForSA = aboutTheEstate.registeredForSA,
      sautr = aboutTheEstate.sautr.map(d),
      address = aboutTheEstate.address.map(decryptAddress(_, sessionId))
    )
  }
}
