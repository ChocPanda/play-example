package io.panda.example.controllers

import akka.http.scaladsl.model.HttpMethods._
import io.panda.example.model.ItemId
import io.panda.example.model.client.requests._
import io.panda.example.model.dto.ExampleDto
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.Logger
import play.api.libs.circe.Circe
import play.api.mvc._

class ExampleController(wcc: ExampleControllerComponents) extends BaseExampleController(wcc) with Circe {

  override protected val logger = Logger(getClass)

  def getExample: Action[AnyContent] = UserAction(GET, "/Example") { request =>
    val userId = request.userId
    ExampleDao.get(userId) match {
      case Some(dto) => Ok(dto.items.asJson)
      case None      => Ok(Set.empty[ItemId].asJson)
    }
  }

  def addItem: Action[AddRequest] = UserAction(PATCH, "/Example")(circe.json[AddRequest]) { addRequest =>
    val userId = addRequest.userId

    val dto = ExampleDao.upsert(ExampleDto(userId, Set(addRequest.body.itemID))) { dto =>
      dto.copy(items = dto.items + addRequest.body.itemID)
    }

    Ok(dto.items.asJson)
  }

  def deleteFromExample: Action[DeleteRequest] = UserAction(DELETE, "/Example")(circe.json[DeleteRequest]) {
    deleteRequest =>
      val userId = deleteRequest.userId

      ExampleDao.update(userId)(
        dto => dto.copy(items = dto.items.filterNot(_ == deleteRequest.body.itemID))
      ) match {
        case Some(dto) => Ok(dto.items.asJson)
        case None      => Ok(Set.empty[ItemId].asJson)
      }
  }
}
