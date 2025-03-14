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
import models.disclosure._
import models.store.FullDisclosure
import models.EncryptedFullDisclosure

@Singleton
class FullDisclosureEncrypter @Inject() (
  crypto: SecureGCMCipher,
  notificationEncrypter: NotificationEncrypter
) {

  def encryptFullDisclosure(fullDisclosure: FullDisclosure, sessionId: String): EncryptedFullDisclosure =
    EncryptedFullDisclosure(
      userId = fullDisclosure.userId,
      submissionId = fullDisclosure.submissionId,
      lastUpdated = fullDisclosure.lastUpdated,
      created = fullDisclosure.created,
      metadata = fullDisclosure.metadata,
      caseReference = encryptCaseReference(fullDisclosure.caseReference, sessionId),
      personalDetails = notificationEncrypter.encryptPersonalDetails(fullDisclosure.personalDetails, sessionId),
      onshoreLiabilities = fullDisclosure.onshoreLiabilities,
      offshoreLiabilities = fullDisclosure.offshoreLiabilities,
      otherLiabilities = fullDisclosure.otherLiabilities,
      reasonForDisclosingNow = encryptReasonForDisclosingNow(fullDisclosure.reasonForDisclosingNow, sessionId),
      customerId = fullDisclosure.customerId,
      madeDeclaration = fullDisclosure.madeDeclaration
    )

  def decryptFullDisclosure(fullDisclosure: EncryptedFullDisclosure, sessionId: String): FullDisclosure =
    FullDisclosure(
      userId = fullDisclosure.userId,
      submissionId = fullDisclosure.submissionId,
      lastUpdated = fullDisclosure.lastUpdated,
      created = fullDisclosure.created,
      metadata = fullDisclosure.metadata,
      caseReference = decryptCaseReference(fullDisclosure.caseReference, sessionId),
      personalDetails = notificationEncrypter.decryptPersonalDetails(fullDisclosure.personalDetails, sessionId),
      onshoreLiabilities = fullDisclosure.onshoreLiabilities,
      offshoreLiabilities = fullDisclosure.offshoreLiabilities,
      otherLiabilities = fullDisclosure.otherLiabilities,
      reasonForDisclosingNow = decryptReasonForDisclosingNow(fullDisclosure.reasonForDisclosingNow, sessionId),
      customerId = fullDisclosure.customerId,
      madeDeclaration = fullDisclosure.madeDeclaration
    )

  def encryptCaseReference(caseReference: CaseReference, sessionId: String): EncryptedCaseReference = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

    EncryptedCaseReference(
      doYouHaveACaseReference = caseReference.doYouHaveACaseReference,
      whatIsTheCaseReference = caseReference.whatIsTheCaseReference.map(e)
    )
  }

  def decryptCaseReference(caseReference: EncryptedCaseReference, sessionId: String): CaseReference = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

    CaseReference(
      doYouHaveACaseReference = caseReference.doYouHaveACaseReference,
      whatIsTheCaseReference = caseReference.whatIsTheCaseReference.map(d)
    )
  }

  def encryptReasonForDisclosingNow(
    reasonForDisclosingNow: ReasonForDisclosingNow,
    sessionId: String
  ): EncryptedReasonForDisclosingNow = {

    def e(field: String): EncryptedValue = crypto.encrypt(field, sessionId)

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
    sessionId: String
  ): ReasonForDisclosingNow = {

    def d(field: EncryptedValue): String = crypto.decrypt(field, sessionId)

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
