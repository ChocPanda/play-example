package io.panda.example.controllers.tools

import cats.implicits._
import io.panda.example.model.UserId
import io.panda.example.model
import play.api.http.HeaderNames
import play.api.mvc.Results.Unauthorized
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

final case class UserRequest[A](userId: UserId, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedAction(override val parser: BodyParser[AnyContent])(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent]
    with ActionRefiner[Request, UserRequest] {

  // This is of course not really secure, in the real world I would make this more secure by
  // passing the token to an OAuth service and getting the user ID from there rather than passing
  // the userID as the token
  protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] =
    Future.successful(
      request.headers
        .get(HeaderNames.BEARER_TOKEN)
        .toRight(Unauthorized("Failed to provide valid bearer token"))
        .flatMap(id => model.validateUserId(id).leftMap(err => Unauthorized(err.message)))
        .map(UserRequest[A](_, request))
    )
}
