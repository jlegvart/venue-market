package io.reality.service

import io.reality.domain.Venue
import io.reality.domain.Player
import io.reality.repository.VenueRepositoryAlgebra
import io.reality.error.ValidationError
import io.reality.error.VenueAlreadyExistsError
import io.reality.error.VenueNotFound

class VenueService private (repository: VenueRepositoryAlgebra) {

  def createVenue(venue: Venue): Either[ValidationError, Venue] =
    repository.getVenueById(venue.id) match {
      case None        => Right(repository.updateVenue(venue))
      case Some(venue) => Left(VenueAlreadyExistsError(venue.id))
    }

  def getVenue(id: String): Option[Venue] = repository.getVenueById(id)

  def getVenues: List[Venue] = repository.getAllVenues

  def deleteVenue(id: String): Either[ValidationError, Venue] =
    repository.deleteVenue(id) match {
      case None        => Left(VenueNotFound(id))
      case Some(venue) => Right(venue)
    }

  def updateVenueOwnership(player: Player, venue: Venue): Venue = repository.updateVenue(
    venue.copy(owner = Some(player.playerId))
  )

}

object VenueService {

  def make(repository: VenueRepositoryAlgebra): VenueService = new VenueService(repository)

}
