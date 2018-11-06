package io.panda.example.test.utils

import io.panda.example.model.dto.ExampleDto
import io.panda.example.model.{ ItemId, UserId }
import org.scalacheck.{ Arbitrary, Gen }

trait ArbitraryInstances {

  implicit protected val arbItemId: Arbitrary[ItemId] = Arbitrary(
    Gen.listOfN(ItemId.length, Gen.alphaNumChar).map(_.mkString).map(testItemId)
  )
  implicit protected val arbUserId: Arbitrary[UserId] = Arbitrary(
    Gen.listOfN(UserId.length, Gen.alphaNumChar).map(_.mkString).map(testUserId)
  )

  implicit protected val arbExampleDto: Arbitrary[ExampleDto] = Arbitrary[ExampleDto] {
    for {
      userId <- arbUserId.arbitrary
      items  <- Gen.containerOf[Set, ItemId](arbItemId.arbitrary)
    } yield ExampleDto(userId, items)
  }

  protected val validUserId: UserId = arbUserId.arbitrary.sample.get
  protected val validItemId: ItemId = arbItemId.arbitrary.sample.get

}
