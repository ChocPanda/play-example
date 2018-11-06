package io.panda.example.model.dao

abstract class DataAccessObject[Key, DTO: Identifiable[Key, ?]] {
  def get(id: Key): Option[DTO]
  def add(dto: DTO): Option[DTO]
  def update(id: Key)(update: DTO => DTO): Option[DTO]
  def upsert(dto: DTO)(update: DTO => DTO): DTO
}
