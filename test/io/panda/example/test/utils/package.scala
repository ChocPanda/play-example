package io.panda.example.test

import io.panda.example.model.{ ItemId, ItemIdTag, UserId, UserIdTag }
import shapeless.tag

import scala.collection.IterableLike
import scala.collection.generic.CanBuildFrom

package object utils {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  implicit class RichCollection[A, Repr](xs: IterableLike[A, Repr]) {
    def distinctBy[B, That](f: A => B)(implicit cbf: CanBuildFrom[Repr, A, That]): That = {
      val builder = cbf(xs.repr)
      val i       = xs.iterator
      var set     = Set[B]()
      while (i.hasNext) {
        val o = i.next
        val b = f(o)
        if (!set(b)) {
          set += b
          builder += o
        }
      }
      builder.result
    }
  }

  def testItemId(rawItemId: String): ItemId = tag[ItemIdTag][String](rawItemId)
  def testUserId(rawUserId: String): UserId = tag[UserIdTag][String](rawUserId)

}
