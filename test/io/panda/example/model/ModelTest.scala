package io.panda.example.model

import io.panda.example.test.utils._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ EitherValues, MustMatchers, WordSpec }

class ModelTest extends WordSpec with MustMatchers with PropertyChecks with EitherValues {

  "UserId validation" should {
    "succeed if the input string is valid" in forAll(
      Table(
        "input",
        "157890385946",
        "gmnlJHOVkfFe",
        "fg8ig5904wb5"
      )
    ) { input =>
      validateUserId(input).right.value mustEqual testUserId(input)
    }

    "fail if the the string has invalid length" in forAll { input: String =>
      whenever(input.length != UserId.length) {
        validateUserId(input) must be('left)
      }
    }

    "fail if the string contains invalid characters" in forAll(
      Table(
        "input",
        "trfe$NOcft7q",
        "f&*_*UMNVDcP",
        "*($(%_£)~~@£"
      )
    ) { input =>
      validateUserId(input) must be('left)
    }
  }

  "ItemId validation" should {
    "succeed if the input string is valid" in forAll(
      Table(
        "input",
        "121863165432",
        "fnnFLlgDFGwe",
        "Fgr58NVbf453"
      )
    ) { input =>
      validateItemId(input).right.value mustEqual testItemId(input)
    }

    "fail if the the string has invalid length" in forAll { input: String =>
      whenever(input.length != ItemId.length) {
        validateItemId(input) must be('left)
      }
    }

    "fail if the string contains invalid characters" in forAll(
      Table(
        "input",
        "gwe$gdus98rf",
        "5n%weF&VD(gv",
        "f%we$@~DFG£4"
      )
    ) { input =>
      validateItemId(input) must be('left)
    }
  }

}
