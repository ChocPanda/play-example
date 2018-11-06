package io.panda.example.controllers

import io.panda.example.controllers.tools.AuthenticatedAction
import io.panda.example.model.UserId
import io.panda.example.model.dao.DataAccessObject
import io.panda.example.model.dao.im.IMDao
import io.panda.example.model.dto.ExampleDto
import play.api.http.FileMimeTypes
import play.api.i18n.{ Langs, MessagesApi }
import play.api.mvc._

import scala.concurrent.ExecutionContext

trait ExampleControllerComponents extends ControllerComponents {
  def ExampleDao: DataAccessObject[UserId, ExampleDto]
  def AuthenticateAction: AuthenticatedAction
}

final case class ExampleControllerComponentsImpl(
  override val ExampleDao: IMDao[UserId, ExampleDto],
  override val AuthenticateAction: AuthenticatedAction,
  override val actionBuilder: ActionBuilder[Request, AnyContent],
  override val parsers: PlayBodyParsers,
  override val messagesApi: MessagesApi,
  override val langs: Langs,
  override val fileMimeTypes: FileMimeTypes,
  override val executionContext: ExecutionContext
) extends ExampleControllerComponents
