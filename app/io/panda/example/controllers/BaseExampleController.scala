package io.panda.example.controllers

import akka.http.scaladsl.model.HttpMethod
import io.panda.example.controllers.tools.{ LoggedAction, UserRequest }
import io.panda.example.model.UserId
import io.panda.example.model.dao.DataAccessObject
import io.panda.example.model.dto.ExampleDto
import play.api.Logger
import play.api.mvc.{ ActionBuilder, AnyContent, BaseController, ControllerComponents }

abstract class BaseExampleController(wcc: ExampleControllerComponents) extends BaseController {

  protected def logger: Logger

  override def controllerComponents: ControllerComponents = wcc

  protected val ExampleDao: DataAccessObject[UserId, ExampleDto] = wcc.ExampleDao

  // This implementation is flawed because requests that fail to authenticate won't be logged... :(
  def UserAction(method: HttpMethod, path: String): ActionBuilder[UserRequest, AnyContent] =
    Action andThen wcc.AuthenticateAction andThen LoggedAction(logger)(method, path)(wcc.executionContext)
}
