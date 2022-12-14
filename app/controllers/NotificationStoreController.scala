/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.mvc.{AnyContent, Action, ControllerComponents}
import javax.inject.{Inject, Singleton}
import repositories.NotificationRepository
import models.notification.Notification
import scala.concurrent.ExecutionContext
import play.api.libs.json.{Json, JsValue}
import uk.gov.hmrc.internalauth.client._
import controllers.Permissions.internalAuthPermission

@Singleton()
class NotificationStoreController @Inject()(
    notificationRepository: NotificationRepository,
    auth: BackendAuthComponents,
    cc: ControllerComponents
  )(implicit ec: ExecutionContext) extends BaseController(cc) {

  val permission = internalAuthPermission("notification")

  def get(userId: String, notificationId: String): Action[AnyContent] = 
    auth.authorizedAction(permission).async { 
      notificationRepository.get(userId, notificationId).map { _ match {
        case Some(notification) => Ok(Json.toJson(notification))
        case None => NotFound("Notification not found")
      }}
    }

  def getAll(userId: String): Action[AnyContent] = 
    auth.authorizedAction(permission).async { 
      notificationRepository.get(userId).map { _ match {
        case Nil => NotFound("Notifications not found")
        case notifications => Ok(Json.toJson(notifications))
      }}
    }

  def set(): Action[JsValue] = 
    auth.authorizedAction(permission).async(parse.json) { implicit request =>
      withValidJson[Notification]{ notification =>
        notificationRepository.set(notification).map(_ => NoContent)
      }
    }

  def delete(userId: String, notificationId: String): Action[JsValue] = 
    auth.authorizedAction(permission).async(parse.json) { _ =>
      notificationRepository.clear(userId, notificationId).map(_ => NoContent)
    }

}
