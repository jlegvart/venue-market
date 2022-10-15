package io.reality.service

import io.reality.repository.PlayerRepositoryAlgebra
import io.reality.domain.Player
import io.reality.error.ValidationError
import io.reality.error.NotEnoughFunds

class PlayerService private (repository: PlayerRepositoryAlgebra) {

  def getPlayer(id: String): Option[Player] = repository.getPlayer(id)

  def reducePlayerFunds(player: Player, amount: BigDecimal): Unit = repository.updatePlayer(
    player.copy(money = player.money - amount)
  )

}

object PlayerService {

  def make(repository: PlayerRepositoryAlgebra): PlayerService = new PlayerService(repository)

}
