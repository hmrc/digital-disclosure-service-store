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
import uk.gov.hmrc.crypto.{AesGcmAdCrypto, EncryptedValue}
import config.AppConfig

@Singleton
class NotificationEncrypter @Inject() (appConfig: AppConfig) {

  private val crypto = new AesGcmAdCrypto(appConfig.mongoEncryptionKey)

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
      organisationName = background.organisationName.map(field => e(field)),
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
      organisationName = background.organisationName.map(field => d(field)),
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
      line2 = address.line2.map(field => e(field)),
      line3 = address.line3.map(field => e(field)),
      line4 = address.line4.map(field => e(field)),
      postcode = address.postcode.map(field => e(field)),
      country = e(address.country.code)
    )
  }

  def decryptAddress(address: EncryptedAddress, sessionId: String): Address = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    Address(
      line1 = d(address.line1),
      line2 = address.line2.map(field => d(field)),
      line3 = address.line3.map(field => d(field)),
      line4 = address.line4.map(field => d(field)),
      postcode = address.postcode.map(field => d(field)),
      country = Country(d(address.country))
    )
  }

  def encryptAboutYou(aboutYou: AboutYou, sessionId: String): EncryptedAboutYou = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutYou(
      fullName = aboutYou.fullName.map(field => e(field)),
      telephoneNumber = aboutYou.telephoneNumber.map(field => e(field)),
      contactPreference = aboutYou.contactPreference,
      emailAddress = aboutYou.emailAddress.map(field => e(field)),
      dateOfBirth = aboutYou.dateOfBirth.map(dob => e(dob.toString)),
      mainOccupation = aboutYou.mainOccupation,
      doYouHaveANino = aboutYou.doYouHaveANino,
      nino = aboutYou.nino.map(field => e(field)),
      registeredForVAT = aboutYou.registeredForVAT,
      vatRegNumber = aboutYou.vatRegNumber.map(field => e(field)),
      registeredForSA = aboutYou.registeredForSA,
      sautr = aboutYou.sautr.map(field => e(field)),
      address = aboutYou.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutYou(aboutYou: EncryptedAboutYou, sessionId: String): AboutYou = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutYou(
      fullName = aboutYou.fullName.map(field => d(field)),
      telephoneNumber = aboutYou.telephoneNumber.map(field => d(field)),
      contactPreference = aboutYou.contactPreference,
      emailAddress = aboutYou.emailAddress.map(field => d(field)),
      dateOfBirth = aboutYou.dateOfBirth.map(dob => LocalDate.parse(d(dob))),
      mainOccupation = aboutYou.mainOccupation,
      doYouHaveANino = aboutYou.doYouHaveANino,
      nino = aboutYou.nino.map(field => d(field)),
      registeredForVAT = aboutYou.registeredForVAT,
      vatRegNumber = aboutYou.vatRegNumber.map(field => d(field)),
      registeredForSA = aboutYou.registeredForSA,
      sautr = aboutYou.sautr.map(field => d(field)),
      address = aboutYou.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheIndividual(
    aboutTheIndividual: AboutTheIndividual,
    sessionId: String
  ): EncryptedAboutTheIndividual = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheIndividual(
      fullName = aboutTheIndividual.fullName.map(field => e(field)),
      dateOfBirth = aboutTheIndividual.dateOfBirth.map(dob => e(dob.toString)),
      mainOccupation = aboutTheIndividual.mainOccupation,
      doTheyHaveANino = aboutTheIndividual.doTheyHaveANino,
      nino = aboutTheIndividual.nino.map(field => e(field)),
      registeredForVAT = aboutTheIndividual.registeredForVAT,
      vatRegNumber = aboutTheIndividual.vatRegNumber.map(field => e(field)),
      registeredForSA = aboutTheIndividual.registeredForSA,
      sautr = aboutTheIndividual.sautr.map(field => e(field)),
      address = aboutTheIndividual.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheIndividual(
    aboutTheIndividual: EncryptedAboutTheIndividual,
    sessionId: String
  ): AboutTheIndividual = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheIndividual(
      fullName = aboutTheIndividual.fullName.map(field => d(field)),
      dateOfBirth = aboutTheIndividual.dateOfBirth.map(dob => LocalDate.parse(d(dob))),
      mainOccupation = aboutTheIndividual.mainOccupation,
      doTheyHaveANino = aboutTheIndividual.doTheyHaveANino,
      nino = aboutTheIndividual.nino.map(field => d(field)),
      registeredForVAT = aboutTheIndividual.registeredForVAT,
      vatRegNumber = aboutTheIndividual.vatRegNumber.map(field => d(field)),
      registeredForSA = aboutTheIndividual.registeredForSA,
      sautr = aboutTheIndividual.sautr.map(field => d(field)),
      address = aboutTheIndividual.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheCompany(aboutTheCompany: AboutTheCompany, sessionId: String): EncryptedAboutTheCompany = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheCompany(
      name = aboutTheCompany.name.map(field => e(field)),
      registrationNumber = aboutTheCompany.registrationNumber.map(field => e(field)),
      address = aboutTheCompany.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheCompany(aboutTheCompany: EncryptedAboutTheCompany, sessionId: String): AboutTheCompany = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheCompany(
      name = aboutTheCompany.name.map(field => d(field)),
      registrationNumber = aboutTheCompany.registrationNumber.map(field => d(field)),
      address = aboutTheCompany.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheTrust(aboutTheTrust: AboutTheTrust, sessionId: String): EncryptedAboutTheTrust = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheTrust(
      name = aboutTheTrust.name.map(field => e(field)),
      address = aboutTheTrust.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheTrust(aboutTheTrust: EncryptedAboutTheTrust, sessionId: String): AboutTheTrust = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheTrust(
      name = aboutTheTrust.name.map(field => d(field)),
      address = aboutTheTrust.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheLLP(aboutTheLLP: AboutTheLLP, sessionId: String): EncryptedAboutTheLLP = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheLLP(
      name = aboutTheLLP.name.map(field => e(field)),
      address = aboutTheLLP.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheLLP(aboutTheLLP: EncryptedAboutTheLLP, sessionId: String): AboutTheLLP = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheLLP(
      name = aboutTheLLP.name.map(field => d(field)),
      address = aboutTheLLP.address.map(decryptAddress(_, sessionId))
    )
  }

  def encryptAboutTheEstate(aboutTheEstate: AboutTheEstate, sessionId: String): EncryptedAboutTheEstate = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedAboutTheEstate(
      fullName = aboutTheEstate.fullName.map(field => e(field)),
      dateOfBirth = aboutTheEstate.dateOfBirth.map(dob => e(dob.toString)),
      mainOccupation = aboutTheEstate.mainOccupation,
      doTheyHaveANino = aboutTheEstate.doTheyHaveANino,
      nino = aboutTheEstate.nino.map(field => e(field)),
      registeredForVAT = aboutTheEstate.registeredForVAT,
      vatRegNumber = aboutTheEstate.vatRegNumber.map(field => e(field)),
      registeredForSA = aboutTheEstate.registeredForSA,
      sautr = aboutTheEstate.sautr.map(field => e(field)),
      address = aboutTheEstate.address.map(encryptAddress(_, sessionId))
    )
  }

  def decryptAboutTheEstate(aboutTheEstate: EncryptedAboutTheEstate, sessionId: String): AboutTheEstate = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    AboutTheEstate(
      fullName = aboutTheEstate.fullName.map(field => d(field)),
      dateOfBirth = aboutTheEstate.dateOfBirth.map(dob => LocalDate.parse(d(dob))),
      mainOccupation = aboutTheEstate.mainOccupation,
      doTheyHaveANino = aboutTheEstate.doTheyHaveANino,
      nino = aboutTheEstate.nino.map(field => d(field)),
      registeredForVAT = aboutTheEstate.registeredForVAT,
      vatRegNumber = aboutTheEstate.vatRegNumber.map(field => d(field)),
      registeredForSA = aboutTheEstate.registeredForSA,
      sautr = aboutTheEstate.sautr.map(field => d(field)),
      address = aboutTheEstate.address.map(decryptAddress(_, sessionId))
    )
  }
}
