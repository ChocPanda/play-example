package io.panda.example.model.dao

trait Identifiable[K, A] {

  type Key = K

  def id(a: A): Key
}

object Identifiable extends IdentifiableSyntax

trait IdentifiableSyntax {
  implicit class Syntax[A](a: A) {
    def id[K](implicit identifiable: Identifiable[K, A]): K = identifiable.id(a)
  }
}
