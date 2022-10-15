package io.reality.repository

import io.reality.domain.Venue

trait VenueRepositoryAlgebra {

  def getAllVenues: List[Venue]

  def getVenueById(id: String): Option[Venue]

  def deleteVenue(id: String): Option[Venue]

  def updateVenue(venue: Venue): Venue

}
