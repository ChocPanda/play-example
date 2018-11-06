package io.panda.example.controllers.tools

import akka.http.scaladsl.model.HttpMethod
import play.api.Logger
import play.api.mvc.{ ActionFunction, Result }

import scala.concurrent.{ ExecutionContext, Future }

case class LoggedAction(logger: Logger)(method: HttpMethod, path: String)(
  implicit override val executionContext: ExecutionContext
) extends ActionFunction[UserRequest, UserRequest] {
  override def invokeBlock[A](request: UserRequest[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    logger.trace(s"Received request: ${method.value} on path: $path for: ${request.userId}")
    block(request) map {
      case res @ Result(header, _, _, _, _) if header.status >= 200 && header.status < 300 =>
        logger.trace(s"Request for user: ${request.userId}, resulted in ${res.header.status}")
        res
      case res =>
        logger.warn(s"Request for user: ${request.userId}, resulted in ${res.header.status}")
        res
    }
  }
}
