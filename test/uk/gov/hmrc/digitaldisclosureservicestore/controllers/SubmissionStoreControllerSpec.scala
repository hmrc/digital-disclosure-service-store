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

package controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import org.mockito.Mockito.when
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar

import java.time.{LocalDateTime, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.json.Json
import models.notification._
import models.store.{Notification, Submission}
import models.Metadata
import play.api.mvc.ControllerComponents
import repositories.SubmissionRepository
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}

class SubmissionStoreControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with MaterializerSpec {

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockSubmissionRepository)
  }

  implicit val cc: ControllerComponents = Helpers.stubControllerComponents()
  val mockSubmissionRepository = mock[SubmissionRepository]  
  val mockStubBehaviour = mock[StubBehaviour]
  val expectedPredicate = Predicate.Permission(Resource(ResourceType("digital-disclosure-service-store"), ResourceLocation("notification")), IAAction("WRITE"))
  when(mockStubBehaviour.stubAuth(Some(expectedPredicate), Retrieval.EmptyRetrieval)).thenReturn(Future.unit)
  private val controller = new SubmissionStoreController(mockSubmissionRepository, BackendAuthComponentsStub(mockStubBehaviour), Helpers.stubControllerComponents())

  val instant = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)
  val testNotification: Submission = Notification("123", "123", instant, instant, Metadata(), PersonalDetails(Background(), AboutYou()))
  
  "GET /notification/user/:userId" should {
    "return 200" in {
      when(mockSubmissionRepository.get("123")) thenReturn Future.successful(Seq(testNotification))

      val fakeRequest = FakeRequest("GET", "/notification/user/123").withHeaders("Authorization" -> "Token some-token")
      val result = controller.getAll("123")(fakeRequest)
      status(result) shouldBe Status.OK
      val body = contentAsJson(result).as[Seq[Notification]]
      body shouldBe Seq(testNotification)
    }

    "return 404" in {
      when(mockSubmissionRepository.get("123")) thenReturn Future.successful(Nil)

      val fakeRequest = FakeRequest("GET", "/notification/123").withHeaders("Authorization" -> "Token some-token")
      val result = controller.getAll("123")(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "GET /notification/user/:userId/id/:id" should {
    "return 200" in {
      when(mockSubmissionRepository.get("123", "456")) thenReturn Future.successful(Some(testNotification))

      val fakeRequest = FakeRequest("GET", "/notification/user/123/id/456").withHeaders("Authorization" -> "Token some-token")
      val result = controller.get("123", "456")(fakeRequest)
      status(result) shouldBe Status.OK
      val body = contentAsJson(result).as[Notification]
      body shouldBe testNotification
    }

    "return 404" in {
      when(mockSubmissionRepository.get("123", "456")) thenReturn Future.successful(None)

      val fakeRequest = FakeRequest("GET", "/notification/user/123/id/456").withHeaders("Authorization" -> "Token some-token")
      val result = controller.get("123", "456")(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "PUT /notification/" should {
    "return 204" in {
      when(mockSubmissionRepository.set(testNotification)) thenReturn Future.successful(true)

      val fakeRequest = FakeRequest(method = "PUT", uri = "/notification", headers = FakeHeaders(Seq("Authorization" -> "Token some-token")), body = Json.toJson(testNotification))
      val result = controller.set()(fakeRequest)
      status(result) shouldBe Status.NO_CONTENT
    }
  }

  "DELETE /notification/user/:userId/id/:id" should {
    "return 204" in {
      when(mockSubmissionRepository.clear("123", "456")) thenReturn Future.successful(true)

      val fakeRequest = FakeRequest(method = "DELETE", uri = "/notification", headers = FakeHeaders(Seq("Authorization" -> "Token some-token")), body = Json.toJson(testNotification))
      val result = controller.delete("123", "456")(fakeRequest)
      status(result) shouldBe Status.NO_CONTENT
    }
  }

}
