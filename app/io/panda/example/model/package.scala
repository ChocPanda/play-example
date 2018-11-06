package io.panda.example

import cats.implicits._
import io.panda.example.model.dao.IdentifiableSyntax
import io.circe._
import shapeless.tag.@@

package object model extends IdentifiableSyntax {

  final case class ValidationError(message: String)
  type ValidationResult[A] = Either[ValidationError, A]

  sealed trait UserIdTag
  type UserId = String @@ UserIdTag

  object UserId {
    val length = 12
  }

  def validateUserId(rawUserId: String): ValidationResult[UserId] = Either.cond(
    rawUserId.length == UserId.length && rawUserId.forall(_.isLetterOrDigit),
    shapeless.tag[UserIdTag][String](rawUserId),
    ValidationError(s"User ID's must be alphanumeric strings of length ${UserId.length}")
  )

  sealed trait ItemIdTag
  type ItemId = String @@ ItemIdTag

  object ItemId {
    val length = 12
  }

  def validateItemId(rawItemId: String): ValidationResult[ItemId] = Either.cond(
    rawItemId.length == ItemId.length && rawItemId.forall(_.isLetterOrDigit),
    shapeless.tag[ItemIdTag][String](rawItemId),
    ValidationError(s"Item ID's must be alphanumeric strings of length ${ItemId.length}")
  )

  implicit val itemIdEncoder: Encoder[ItemId] = (a: ItemId) => Json.fromString(a.toString)
  implicit val itemIdDecoder: Decoder[ItemId] = (cursor: HCursor) =>
    cursor.focus.flatMap(_.asString) match {
      case Some(str) => validateItemId(str).leftMap(err => DecodingFailure(err.message, Nil))
      case _         => Left(DecodingFailure(s"Item ID's must be alphanumeric strings of length ${ItemId.length}", Nil))
  }

  type Identifiable[K, A] = io.panda.example.model.dao.Identifiable[K, A]
  val Identifiable = io.panda.example.model.dao.Identifiable

}
