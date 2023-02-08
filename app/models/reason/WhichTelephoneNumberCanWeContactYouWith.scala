package models

sealed trait WhichTelephoneNumberCanWeContactYouWith

object WhichTelephoneNumberCanWeContactYouWith extends Enumerable.Implicits {

  case object ExistingNumber extends WithName("existingNumber") with WhichTelephoneNumberCanWeContactYouWith
  case object DifferentNumber extends WithName("differentNumber") with WhichTelephoneNumberCanWeContactYouWith

  val values: Seq[WhichTelephoneNumberCanWeContactYouWith] = Seq(
    ExistingNumber, DifferentNumber
  )

  implicit val enumerable: Enumerable[WhichTelephoneNumberCanWeContactYouWith] =
    Enumerable(values.map(v => v.toString -> v): _*)
}