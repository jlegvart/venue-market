package io.reality.domain

final case class Venue(id: String, name: String, price: BigDecimal, owner: Option[String])
final case class Player(playerId: String, money: BigDecimal)