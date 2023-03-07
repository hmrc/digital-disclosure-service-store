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

import com.google.inject.{Inject, Singleton}
import models.disclosure._
import models.store.FullDisclosure
import models.EncryptedFullDisclosure

@Singleton
class FullDisclosureEncrypter @Inject()(
  crypto: SecureGCMCipher,
  notificationEncrypter: NotificationEncrypter
) {

  def encryptFullDisclosure(
    fullDisclosure: FullDisclosure,
    sessionId: String,
    key: String): EncryptedFullDisclosure = {

    EncryptedFullDisclosure(
      userId = fullDisclosure.userId,
      submissionId = fullDisclosure.submissionId,
      lastUpdated = fullDisclosure.lastUpdated,
      created = fullDisclosure.created,
      metadata = fullDisclosure.metadata,
      caseReference = encryptCaseReference(fullDisclosure.caseReference, sessionId, key),
      personalDetails = notificationEncrypter.encryptPersonalDetails(fullDisclosure.personalDetails, sessionId, key),
      offshoreLiabilities = fullDisclosure.offshoreLiabilities,
      otherLiabilities = fullDisclosure.otherLiabilities,
      reasonForDisclosingNow = encryptReasonForDisclosingNow(fullDisclosure.reasonForDisclosingNow, sessionId, key),
      customerId = fullDisclosure.customerId,
      madeDeclaration = fullDisclosure.madeDeclaration
    )
  } 

  def decryptFullDisclosure(
    fullDisclosure: EncryptedFullDisclosure,
    sessionId: String,
    key: String): FullDisclosure = {

    FullDisclosure(
      userId = fullDisclosure.userId,
      submissionId = fullDisclosure.submissionId,
      lastUpdated = fullDisclosure.lastUpdated,
      created = fullDisclosure.created,
      metadata = fullDisclosure.metadata,
      caseReference = decryptCaseReference(fullDisclosure.caseReference, sessionId, key),
      personalDetails = notificationEncrypter.decryptPersonalDetails(fullDisclosure.personalDetails, sessionId, key),
      offshoreLiabilities = fullDisclosure.offshoreLiabilities,
      otherLiabilities = fullDisclosure.otherLiabilities,
      reasonForDisclosingNow = decryptReasonForDisclosingNow(fullDisclosure.reasonForDisclosingNow, sessionId, key),
      customerId = fullDisclosure.customerId,
      madeDeclaration = fullDisclosure.madeDeclaration
    )
  }

  def encryptCaseReference(
    caseReference: CaseReference,
    sessionId: String,
    key: String): EncryptedCaseReference = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId, key)

    EncryptedCaseReference (
      doYouHaveACaseReference = caseReference.doYouHaveACaseReference,
      whatIsTheCaseReference = caseReference.whatIsTheCaseReference.map(e)
    )
  } 

  def decryptCaseReference(
    caseReference: EncryptedCaseReference,
    sessionId: String,
    key: String): CaseReference = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId, key)

    CaseReference (
      doYouHaveACaseReference = caseReference.doYouHaveACaseReference,
      whatIsTheCaseReference = caseReference.whatIsTheCaseReference.map(d)
    )
  }

  def encryptReasonForDisclosingNow(
    reasonForDisclosingNow: ReasonForDisclosingNow,
    sessionId: String,
    key: String): EncryptedReasonForDisclosingNow = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId, key)

    EncryptedReasonForDisclosingNow(
      reason = reasonForDisclosingNow.reason,
      otherReason = reasonForDisclosingNow.otherReason,
      whyNotBeforeNow = reasonForDisclosingNow.whyNotBeforeNow,
      receivedAdvice = reasonForDisclosingNow.receivedAdvice,
      personWhoGaveAdvice = reasonForDisclosingNow.personWhoGaveAdvice.map(e),
      adviceOnBehalfOfBusiness = reasonForDisclosingNow.adviceOnBehalfOfBusiness,
      adviceBusinessName = reasonForDisclosingNow.adviceBusinessName.map(e),
      personProfession = reasonForDisclosingNow.personProfession,
      adviceGiven = reasonForDisclosingNow.adviceGiven,
      whichEmail = reasonForDisclosingNow.whichEmail,
      whichPhone = reasonForDisclosingNow.whichPhone,
      email = reasonForDisclosingNow.email.map(e),
      telephone = reasonForDisclosingNow.telephone.map(e)
    )
  } 

  def decryptReasonForDisclosingNow(
    reasonForDisclosingNow: EncryptedReasonForDisclosingNow,
    sessionId: String,
    key: String): ReasonForDisclosingNow = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId, key)

    ReasonForDisclosingNow(
      reason = reasonForDisclosingNow.reason,
      otherReason = reasonForDisclosingNow.otherReason,
      whyNotBeforeNow = reasonForDisclosingNow.whyNotBeforeNow,
      receivedAdvice = reasonForDisclosingNow.receivedAdvice,
      personWhoGaveAdvice = reasonForDisclosingNow.personWhoGaveAdvice.map(d),
      adviceOnBehalfOfBusiness = reasonForDisclosingNow.adviceOnBehalfOfBusiness,
      adviceBusinessName = reasonForDisclosingNow.adviceBusinessName.map(d),
      personProfession = reasonForDisclosingNow.personProfession,
      adviceGiven = reasonForDisclosingNow.adviceGiven,
      whichEmail = reasonForDisclosingNow.whichEmail,
      whichPhone = reasonForDisclosingNow.whichPhone,
      email = reasonForDisclosingNow.email.map(d),
      telephone = reasonForDisclosingNow.telephone.map(d)
    )
  }

}
