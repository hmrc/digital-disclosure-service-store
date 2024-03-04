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
import models.disclosure._
import models.notification._
import models.store._
import models._
import play.api.Configuration

import java.time.{LocalDate, LocalDateTime, ZoneOffset}

class FullDisclosureEncrypterSpec extends AnyFreeSpec with Matchers {

  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  lazy implicit val appConfig: AppConfig = new AppConfig(Configuration("mongodb.encryption.key" -> secretKey))
  private val encrypter = new SecureGCMCipherImpl
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
        created = instant,
        metadata = Metadata(),
        caseReference = CaseReference(),
        personalDetails = PersonalDetails(Background(), AboutYou()),
        onshoreLiabilities = Some(OnshoreLiabilities()),
        offshoreLiabilities = OffshoreLiabilities(),
        otherLiabilities = OtherLiabilities(),
        reasonForDisclosingNow = ReasonForDisclosingNow(),
        customerId = None
      )   

      val encryptedModel = sut.encryptFullDisclosure(model, associatedText)

      encryptedModel.userId mustEqual model.userId
      encryptedModel.submissionId mustEqual model.submissionId
      encryptedModel.lastUpdated mustEqual model.lastUpdated
      encryptedModel.metadata mustEqual model.metadata
      encryptedModel.onshoreLiabilities mustEqual model.onshoreLiabilities
      encryptedModel.offshoreLiabilities mustEqual model.offshoreLiabilities
      encryptedModel.otherLiabilities mustEqual model.otherLiabilities

      sut.decryptFullDisclosure(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt a CaseReference" in {

      val model = CaseReference (
        doYouHaveACaseReference = Some(true),
        whatIsTheCaseReference = Some(textToEncrypt)
      ) 

      val encryptedModel = sut.encryptCaseReference(model, associatedText)

      encryptedModel.doYouHaveACaseReference mustEqual model.doYouHaveACaseReference
      encryptedModel.whatIsTheCaseReference.get.value must not equal model.whatIsTheCaseReference.get

      sut.decryptCaseReference(encryptedModel, associatedText) mustEqual model
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

      val encryptedModel = sut.encryptReasonForDisclosingNow(model, associatedText)

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

      sut.decryptReasonForDisclosingNow(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt a full disclosure with onshore liabilities" in {
      val date = LocalDate.now
      val liabilities = OnshoreTaxYearLiabilities(
        lettingIncome = Some(BigInt(2000)),
        gains = Some(BigInt(2000)),
        unpaidTax = BigInt(2000),
        niContributions = BigInt(2000),
        interest = BigInt(2000),
        penaltyRate = 12,
        penaltyRateReason = "Reason",
        undeclaredIncomeOrGain = Some("Some gain"),
        residentialTaxReduction = Some(false)
      )
      val whySet: Set[WhyAreYouMakingThisOnshoreDisclosure] = Set(WhyAreYouMakingThisOnshoreDisclosure.DidNotNotifyHasExcuse)
      val yearsSet: Set[OnshoreYears] = Set(OnshoreYearStarting(2012), PriorToThreeYears, PriorToFiveYears, PriorToNineteenYears)
      val corporationTax = Set(CorporationTaxLiability (
        periodEnd = date,
        howMuchIncome = BigInt(2000),
        howMuchUnpaid = BigInt(2000),
        howMuchInterest = BigInt(2000),
        penaltyRate = 123,
        penaltyRateReason = "Some reason"
      ))
      val directorLoan = Set(DirectorLoanAccountLiabilities (
        name = "Name",
        periodEnd = date,
        overdrawn = BigInt(2000),
        unpaidTax = BigInt(2000),
        interest = BigInt(2000),
        penaltyRate = 123,
        penaltyRateReason = "Some reason"
      ))
      val lettingProperty = Seq(LettingProperty(
        address = None,
        dateFirstLetOut = Some(date),
        stoppedBeingLetOut = Some(true),
        noLongerBeingLetOut = None,
        fhl = Some(false),
        isJointOwnership = Some(true),
        isMortgageOnProperty = Some(false),
        percentageIncomeOnProperty = Some(123),
        wasFurnished = Some(false),
        typeOfMortgage = None,
        otherTypeOfMortgage = Some("Some mortgage"),
        wasPropertyManagerByAgent = Some(true),
        didTheLettingAgentCollectRentOnYourBehalf = Some(false)
      ))
      val whichLiabilitiesSet: Set[WhatOnshoreLiabilitiesDoYouNeedToDisclose] = Set(WhatOnshoreLiabilitiesDoYouNeedToDisclose.BusinessIncome)
      val onshoreLiabilities = OnshoreLiabilities(
        behaviour = Some(whySet), 
        excuseForNotNotifying = Some(ReasonableExcuseOnshore("Some excuse", "Some years")), 
        reasonableCare = Some(ReasonableCareOnshore("Some excuse", "Some years")), 
        excuseForNotFiling = Some(ReasonableExcuseForNotFilingOnshore("Some excuse", "Some years")), 
        whatLiabilities = Some(whichLiabilitiesSet),
        whichYears = Some(yearsSet), 
        youHaveNotIncludedTheTaxYear = Some("Not included year"),
        youHaveNotSelectedCertainTaxYears = Some("Not included years"),
        taxBeforeThreeYears = Some("Some liabilities 1"),
        taxBeforeFiveYears = Some("Some liabilities 2"),
        taxBeforeNineteenYears = Some("Some liabilities 3"),
        disregardedCDF = Some(true),
        taxYearLiabilities = Some(Map("2012" -> OnshoreTaxYearWithLiabilities(OnshoreYearStarting(2012), liabilities))),
        lettingDeductions = Some(Map("2012" -> BigInt(123))),
        lettingProperties = Some(lettingProperty),
        memberOfLandlordAssociations = Some(true),
        landlordAssociations = Some("Some associations"),
        howManyProperties = Some("Some properties"),
        corporationTaxLiabilities = Some(corporationTax),
        directorLoanAccountLiabilities = Some(directorLoan)
      )
      val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
      val model = FullDisclosure(
        userId = textToEncrypt,
        submissionId = textToEncrypt,
        lastUpdated = instant,
        created = instant,
        metadata = Metadata(),
        caseReference = CaseReference(),
        personalDetails = PersonalDetails(Background(), AboutYou()),
        onshoreLiabilities = Some(onshoreLiabilities),
        offshoreLiabilities = OffshoreLiabilities(),
        otherLiabilities = OtherLiabilities(),
        reasonForDisclosingNow = ReasonForDisclosingNow(),
        customerId = None
      )   

      val encryptedModel = sut.encryptFullDisclosure(model, associatedText)
      encryptedModel.onshoreLiabilities mustEqual model.onshoreLiabilities
      sut.decryptFullDisclosure(encryptedModel, associatedText) mustEqual model
    }

    "must encrypt/decrypt a full disclosure with offshore liabilities" in {

      val liabilities = TaxYearLiabilities(
        income = BigInt(2000),
        chargeableTransfers = BigInt(2000),
        capitalGains = BigInt(2000),
        unpaidTax = BigInt(2000),
        interest = BigInt(2000),
        penaltyRate = 12,
        penaltyRateReason = "Reason",
        undeclaredIncomeOrGain = Some("Some gain"),
        foreignTaxCredit = false
      )
      val whySet: Set[WhyAreYouMakingThisDisclosure] = Set(WhyAreYouMakingThisDisclosure.DidNotNotifyHasExcuse)
      val yearsSet: Set[OffshoreYears] = Set(TaxYearStarting(2012), ReasonableExcusePriorTo, CarelessPriorTo, DeliberatePriorTo)
      val interpretationSet: Set[YourLegalInterpretation] = Set(YourLegalInterpretation.AnotherIssue)
      val offshoreLiabilities = OffshoreLiabilities(
        behaviour = Some(whySet), 
        excuseForNotNotifying = Some(WhatIsYourReasonableExcuse("Some excuse", "Some years")), 
        reasonableCare = Some(WhatReasonableCareDidYouTake("Some excuse", "Some years")), 
        excuseForNotFiling = Some(WhatIsYourReasonableExcuseForNotFilingReturn("Some excuse", "Some years")), 
        whichYears = Some(yearsSet), 
        youHaveNotIncludedTheTaxYear = Some("Some value"),
        youHaveNotSelectedCertainTaxYears = Some("Some value"),
        taxBeforeFiveYears = Some("Some liabilities"),
        taxBeforeSevenYears = Some("Some liabilities"),
        taxBeforeNineteenYears = Some("Some liabilities"),
        disregardedCDF = Some(true),
        taxYearLiabilities = Some(Map("2012" -> TaxYearWithLiabilities(TaxYearStarting(2012), liabilities))),
        taxYearForeignTaxDeductions = Some(Map("2012" -> BigInt(123))),
        countryOfYourOffshoreLiability = None,
        legalInterpretation = Some(interpretationSet),
        otherInterpretation = Some("Some interpretation"),
        notIncludedDueToInterpretation = Some(HowMuchTaxHasNotBeenIncluded.TenThousandOrLess),
        maximumValueOfAssets = Some(TheMaximumValueOfAllAssets.Below500k)
      )
      val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
      val model = FullDisclosure(
        userId = textToEncrypt,
        submissionId = textToEncrypt,
        lastUpdated = instant,
        created = instant,
        metadata = Metadata(),
        caseReference = CaseReference(),
        personalDetails = PersonalDetails(Background(), AboutYou()),
        onshoreLiabilities = Some(OnshoreLiabilities()),
        offshoreLiabilities = offshoreLiabilities,
        otherLiabilities = OtherLiabilities(),
        reasonForDisclosingNow = ReasonForDisclosingNow(),
        customerId = None
      )   

      val encryptedModel = sut.encryptFullDisclosure(model, associatedText)
      encryptedModel.onshoreLiabilities mustEqual model.onshoreLiabilities
      sut.decryptFullDisclosure(encryptedModel, associatedText) mustEqual model
    }

  }

}
