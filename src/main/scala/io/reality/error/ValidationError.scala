package io.reality.error

sealed trait ValidationError                                    extends Product with Serializable
case class VenueAlreadyExistsError(id: String)                  extends ValidationError
case class VenueNotFound(id: String)                            extends ValidationError
case class PlayerNotFound(id: String)                           extends ValidationError
case class NotEnoughFunds(playerId: String, venueId: String)    extends ValidationError
case class VenueAlreadyOwned(playerId: String, venueId: String) extends ValidationError