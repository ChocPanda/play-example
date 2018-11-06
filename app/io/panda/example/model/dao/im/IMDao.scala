package io.panda.example.model.dao.im

import java.util.concurrent.ConcurrentHashMap

import io.panda.example.model._
import io.panda.example.model.dao.DataAccessObject

class IMDao[Key, DTO: Identifiable[Key, ?]] extends DataAccessObject[Key, DTO] {

  private val dataStore: ConcurrentHashMap[Key, DTO] =
    new ConcurrentHashMap[Key, DTO]()

  def get(id: Key): Option[DTO] = Option(dataStore.get(id))

  def add(dto: DTO): Option[DTO] =
    Option(dataStore.putIfAbsent(dto.id, dto)) orElse Option(dto)

  def update(id: Key)(update: DTO => DTO): Option[DTO] =
    Option(dataStore.computeIfPresent(id, (_, dto) => update(dto)))

  def upsert(newDto: DTO)(update: DTO => DTO): DTO =
    dataStore.merge(newDto.id, newDto, (oldDto, _) => update(oldDto))

}
