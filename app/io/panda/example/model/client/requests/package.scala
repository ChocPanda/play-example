package io.panda.example.model.client

import io.panda.example.model.ItemId

package object requests {

  final case class AddRequest(itemID: ItemId)
  final case class DeleteRequest(itemID: ItemId)
}
