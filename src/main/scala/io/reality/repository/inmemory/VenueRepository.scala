package io.reality.repository.inmemory

import io.reality.domain.Venue
import io.reality.repository.VenueRepositoryAlgebra
import scala.collection.concurrent.Map

class VenueRepository private (cache: Map[String, Venue]) extends VenueRepositoryAlgebra {

  def getAllVenues: List[Venue] = cache.values.toList

  def getVenueById(id: String): Option[Venue] = cache.get(id)

  def deleteVenue(id: String): Option[Venue] = cache.remove(id)

  def updateVenue(venue: Venue): Venue = {
    cache.update(venue.id, venue)
    venue
  }

}

object VenueRepository {

  def make(cache: Map[String, Venue]): VenueRepository = new VenueRepository(cache)

}
