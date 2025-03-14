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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import javax.inject.{Inject, Singleton}
import repositories.SubmissionRepository
import models.store.Submission
import scala.concurrent.ExecutionContext
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.internalauth.client._
import controllers.Permissions.internalAuthPermission

@Singleton()
class SubmissionStoreController @Inject() (
  submissionRepository: SubmissionRepository,
  auth: BackendAuthComponents,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController(cc) {

  val permission = internalAuthPermission("notification")

  def get(userId: String, submissionId: String): Action[AnyContent] =
    auth.authorizedAction(permission).async {
      submissionRepository.get(userId, submissionId).map {
        _ match {
          case Some(submission) => Ok(Json.toJson(submission))
          case None             => NotFound("Submission not found")
        }
      }
    }

  def getAll(userId: String): Action[AnyContent] =
    auth.authorizedAction(permission).async {
      submissionRepository.get(userId).map {
        _ match {
          case Nil         => NotFound("Submissions not found")
          case submissions => Ok(Json.toJson(submissions))
        }
      }
    }

  def set(): Action[JsValue] =
    auth.authorizedAction(permission).async(parse.json) { implicit request =>
      withValidJson[Submission] { submission =>
        submissionRepository.set(submission).map(_ => NoContent)
      }
    }

  def delete(userId: String, submissionId: String): Action[JsValue] =
    auth.authorizedAction(permission).async(parse.json) { _ =>
      submissionRepository.clear(userId, submissionId).map(_ => NoContent)
    }

}
