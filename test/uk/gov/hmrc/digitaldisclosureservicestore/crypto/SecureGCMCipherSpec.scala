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

import java.security.InvalidAlgorithmParameterException
import java.util.Base64
import javax.crypto.{Cipher, IllegalBlockSizeException, KeyGenerator, NoSuchPaddingException}
import javax.crypto.spec.GCMParameterSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import play.api.Configuration

class SecureGCMCipherSpec extends AnyFreeSpec with Matchers {

  private val secretKey = "zjWYSlNW79BKWTONyGFQsT7buBcWiiOkx8blzp6LNVw="
  private val previousKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val textToEncrypt = "textNotEncrypted"
  private val associatedText = "associatedText"
  private val encryptedText = EncryptedValue(
    "yyOKSD/XoUFKjW5sTbV9VRLiAaT9hznKMTBcZRaTyXE=",
    "lgKRUqUE4SELI2T9YW3Z6DC38tNRG0sgsKEwQ9HDnulzuPOl3nHV56buIhglPqZve7Q+BKrm3/61Yuo3M1rsya0Km7NF9aozNG0E+M6uHEHQANDu+J5gz3zaSNwInuvf"
  )

  implicit val appConfig: AppConfig = new AppConfig(Configuration("mongodb.encryption.key" -> secretKey))
  private val encrypter = new SecureGCMCipherImpl

  val appConfigPrevious: AppConfig = new AppConfig(Configuration("mongodb.encryption.key" -> previousKey))
  val encrypterWithPreviousKey = new SecureGCMCipherImpl()(appConfigPrevious)

  "encrypt" - {

    "must encrypt some text" in {
      val encryptedValue = encrypter.encrypt(textToEncrypt, associatedText)
      encryptedValue mustBe an[EncryptedValue]
    }

    "return an EncryptionDecryptionError if the algorithm is invalid" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override val ALGORITHM_TO_TRANSFORM_STRING: String = "invalid"
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Algorithm being requested is not available in this environment: AES for encrypt"
    }

    "return an EncryptionDecryptionError if the padding is invalid" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override def getCipherInstance: Cipher = throw new NoSuchPaddingException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Padding Scheme being requested is not available this environment for encrypt"
    }

    "return an EncryptionDecryptionError if an InvalidAlgorithmParameterException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override def getCipherInstance: Cipher = throw new InvalidAlgorithmParameterException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Algorithm parameters being specified are not valid for encrypt"
    }

    "return an EncryptionDecryptionError if a IllegalStateException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override def getCipherInstance: Cipher = throw new IllegalStateException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Cipher is in an illegal state for encrypt"
    }

    "return an EncryptionDecryptionError if a UnsupportedOperationException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override def getCipherInstance: Cipher = throw new UnsupportedOperationException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Provider might not be supporting this method for encrypt"
    }

    "return an EncryptionDecryptionError if a IllegalBlockSizeException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override def getCipherInstance: Cipher = throw new IllegalBlockSizeException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Error occurred due to block size for encrypt"
    }

    "return an EncryptionDecryptionError if a RuntimeException is thrown" in {
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override def getCipherInstance: Cipher = throw new RuntimeException()
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.encrypt(textToEncrypt, associatedText)
      )

      encryptedAttempt.failureReason mustBe "Unexpected exception for encrypt"
    }

    "return an EncryptionDecryptionError if the secret key is an invalid type" in {

      val keyGen = KeyGenerator.getInstance("DES")
      val key = keyGen.generateKey()
      val secureGCMEncryter = new SecureGCMCipherImpl {
        override val ALGORITHM_KEY: String = "DES"
      }
      val encryptedAttempt = intercept[EncryptionDecryptionException](
        secureGCMEncryter.generateCipherText(
          textToEncrypt,
          associatedText.getBytes,
          new GCMParameterSpec(96, "hjdfbhvbhvbvjvjfvb".getBytes),
          key)
      )

      encryptedAttempt.failureReason mustBe "Key being used is not valid. " +
        "It could be due to invalid encoding, wrong length or uninitialized for encrypt"
    }
  }

  "decrypt" - {

    "must decrypt text when the same associatedText, nonce and secretKey were used to encrypt it" in {
      val decryptedText = encrypter.decrypt(encryptedText, associatedText)
      decryptedText mustEqual textToEncrypt
    }

    "must decrypt text when a previous key listed in config was used to encrypt it" in {
      lazy val appConfWithBoth: AppConfig = new AppConfig(Configuration(
        "mongodb.encryption.key" -> secretKey,
        "mongodb.encryption.previousKey" -> previousKey
      ))
      val encrypterWithBothKeys = new SecureGCMCipherImpl()(appConfWithBoth)

      val previouslyEncryptedValue = encrypterWithPreviousKey.encrypt(textToEncrypt, associatedText)
      val decryptedText = encrypterWithBothKeys.decrypt(previouslyEncryptedValue, associatedText)
      decryptedText mustEqual textToEncrypt
    }

    "must return an EncryptionDecryptionException if the value was encrypted by a key no longer listed in config" in {
      val previouslyEncryptedValue = encrypterWithPreviousKey.encrypt(textToEncrypt, associatedText)

      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(previouslyEncryptedValue, associatedText)
      )

      decryptAttempt.failureReason mustBe "Error occurred due to padding scheme for decrypt"
    }

    "must return an EncryptionDecryptionException if the encrypted value is different" in {
      val invalidText = Base64.getEncoder.encodeToString("invalid value".getBytes)
      val invalidEncryptedValue = EncryptedValue(invalidText, encryptedText.nonce)

      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(invalidEncryptedValue, associatedText)
      )

      decryptAttempt.failureReason mustBe "Error occurred due to padding scheme for decrypt"
    }

    "must return an EncryptionDecryptionException if the nonce is different" in {
      val invalidNonce = Base64.getEncoder.encodeToString("invalid value".getBytes)
      val invalidEncryptedValue = EncryptedValue(encryptedText.value, invalidNonce)

      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(invalidEncryptedValue, associatedText)
      )

      decryptAttempt.failureReason mustBe "Error occurred due to padding scheme for decrypt"
    }

    "must return an EncryptionDecryptionException if the associated text is different" in {
      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(encryptedText, "invalid associated text")
      )

      decryptAttempt.failureReason mustBe "Error occurred due to padding scheme for decrypt"
    }

    "must return an EncryptionDecryptionException if the associated text is empty" in {
      val decryptAttempt = intercept[EncryptionDecryptionException](
        encrypter.decrypt(encryptedText, "")
      )

      decryptAttempt.failureReason mustBe "associated text must not be null for decrypt"
    }
  }
}
