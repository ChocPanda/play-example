package io.panda.example.model.dto

import io.panda.example.model.dao.Identifiable
import io.panda.example.model.{ ItemId, UserId }

final case class ExampleDto(id: UserId, items: Set[ItemId])

object ExampleDto {

  implicit val identifiableExample: Identifiable[UserId, ExampleDto] =
    new Identifiable[UserId, ExampleDto] {
      override def id(a: ExampleDto): UserId = a.id
    }

}
