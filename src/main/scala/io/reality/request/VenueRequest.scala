package io.reality.request

import io.reality.domain.Venue

final case class VenueRequest(name: String, price: BigDecimal)