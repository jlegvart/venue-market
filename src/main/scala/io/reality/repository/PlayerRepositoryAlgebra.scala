package io.reality.repository

import io.reality.domain.Player

trait PlayerRepositoryAlgebra {

  def getPlayer(id: String): Option[Player]

  def updatePlayer(player: Player)

}
