package io.reality.service

import io.reality.domain.Player
import io.reality.domain.Venue
import io.reality.error.ValidationError
import io.reality.error.NotEnoughFunds
import io.reality.error.VenueAlreadyOwned

class MarketService private (venueService: VenueService, playerService: PlayerService) {

  def buyVenue(player: Player, venue: Venue): Either[ValidationError, Venue] =
    for {
      _            <- validateEnoughFunds(player, venue)
      _            <- validateVenueOwner(player, venue)
      _            <- Right(playerService.reducePlayerFunds(player, venue.price))
      updatedVenue <- Right(venueService.updateVenueOwnership(player, venue))
    } yield updatedVenue

  private def validateEnoughFunds(player: Player, venue: Venue): Either[ValidationError, Unit] =
    if (venue.price > player.money)
      Left(NotEnoughFunds(player.playerId, venue.name))
    else
      Right(())

  private def validateVenueOwner(player: Player, venue: Venue): Either[ValidationError, Unit] =
    venue.owner match {
      case None => Right(())
      case Some(owner) =>
        if (owner == player.playerId)
          Left(VenueAlreadyOwned(player.playerId, venue.name))
        else
          Right(())
    }

}

object MarketService {

  def make(venueService: VenueService, playerService: PlayerService) =
    new MarketService(venueService, playerService)

}
