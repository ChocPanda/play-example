package io.panda.example.model.dao.im

import io.panda.example.model.UserId
import io.panda.example.model.dto.ExampleDto
import io.panda.example.test.utils._
import org.scalatest.{ Inspectors, MustMatchers, OptionValues, WordSpec }
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks

class IMDaoSpec
    extends WordSpec
    with OptionValues
    with MustMatchers
    with PropertyChecks
    with MockitoSugar
    with ArbitraryInstances {

  "IMDaoImpl" should {

    "support adding a data transfer object to the data store and retrieving the object by it's id" in forAll {
      dto: ExampleDto =>
        val dao = new IMDao[UserId, ExampleDto]()

        dao.add(dto).value mustEqual dto
        dao.get(dto.id).value mustEqual dto
    }

    "support adding multiple data transfer objects to the data store and retrieving all the objects by their ids" in forAll {
      dtos: List[ExampleDto] =>
        val distinctListings = dtos.distinctBy(_.id)

        val dao = new IMDao[UserId, ExampleDto]()

        Inspectors.forAll(distinctListings) { dto: ExampleDto =>
          dao.add(dto).value mustEqual dto
          dao.get(dto.id).value mustEqual dto
        }

        // Ensure that the dao is storing more than 1 element
        Inspectors.forAll(distinctListings) { dto: ExampleDto =>
          dao.get(dto.id).value mustEqual dto
        }
    }

    "support updating objects in the data store" in forAll { dto: ExampleDto =>
      whenever(dto.items.nonEmpty) {
        val dao        = new IMDao[UserId, ExampleDto]()
        val updatedDto = ExampleDto(dto.id, Set.empty)

        dao.add(dto).value mustEqual dto
        dao.update(dto.id)(_.copy(items = Set.empty)).value mustEqual updatedDto
        dao.get(dto.id).value mustEqual updatedDto
      }
    }

    "support upserting objects in the data store" in forAll { dto: ExampleDto =>
      whenever(dto.items.nonEmpty) {
        val dao        = new IMDao[UserId, ExampleDto]()
        val updatedDto = ExampleDto(dto.id, Set.empty)

        dao.upsert(dto)(identity) mustEqual dto
        dao.get(dto.id).value mustEqual dto

        dao.upsert(dto)(_.copy(items = Set.empty)) mustEqual updatedDto
        dao.get(dto.id).value mustEqual updatedDto
      }
    }

    "return None when reading a listing which doesn't exist" in {

      val dao = new IMDao[UserId, ExampleDto]()

      dao.get(validUserId) mustEqual None
    }
  }

}
