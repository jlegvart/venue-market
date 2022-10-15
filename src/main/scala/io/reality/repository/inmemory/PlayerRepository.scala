package io.reality.repository.inmemory

import io.reality.domain.Player
import io.reality.repository.PlayerRepositoryAlgebra
import scala.collection.concurrent.Map

class PlayerRepository private (cache: Map[String, Player]) extends PlayerRepositoryAlgebra {

  def getPlayer(id: String): Option[Player] = cache.get(id)

  def updatePlayer(player: Player) = cache.update(player.playerId, player)

}

object PlayerRepository {

  def make(cache: Map[String, Player]): PlayerRepository = new PlayerRepository(cache)

}
