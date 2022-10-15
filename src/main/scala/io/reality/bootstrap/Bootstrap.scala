package io.reality.bootstrap

import scala.collection.concurrent.TrieMap
import io.reality.domain.Player

object Bootstrap {

  def playerData: TrieMap[String, Player] = {
    val player1 = Player("player1", 500)
    val player2 = Player("player2", 2000)

    val cache = new TrieMap[String, Player]()
    cache.put(player1.playerId, player1)
    cache.put(player2.playerId, player2)
    cache
  }

}
