package io.reality.controller

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import io.reality.domain._
import io.reality.error._
import io.reality.request.VenueRequest
import io.reality.service.MarketService
import io.reality.service.PlayerService
import io.reality.service.VenueService
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

class VenueController private (
  venueService: VenueService,
  marketService: MarketService,
  playerService: PlayerService,
) {

  private val log = LoggerFactory.getLogger(this.getClass().getName())

  implicit val playerForm   = jsonFormat2(Player)
  implicit val venueForm    = jsonFormat4(Venue)
  implicit val venueRForm   = jsonFormat2(VenueRequest)
  implicit val purchaseForm = jsonFormat1(Purchase)

  private val getVenues = get {
    log.debug("Get venues")
    complete(venueService.getVenues)
  }

  private val createVenue =
    path(Segment) { id: String =>
      put {
        log.debug(s"Create new venue: $id")
        entity(as[VenueRequest]) { request =>
          venueService.createVenue(Venue(id, request.name, request.price, None)) match {
            case Left(err)    => handleError(err)
            case Right(value) => complete(StatusCodes.Created, id)
          }
        }
      }
    }

  private val deleteVenue =
    path(Segment) { id: String =>
      delete {
        log.debug(s"Delete venue: $id")
        venueService.deleteVenue(id) match {
          case Left(err)    => handleError(err)
          case Right(venue) => complete(venue.id)
        }
      }
    }

  private val buyVenue =
    path(Segment / "buy") { id: String =>
      post {
        log.debug(s"Buy venue: $id")
        entity(as[Purchase]) { request =>
          val action =
            for {
              venue    <- getVenue(id)
              player   <- getPlayer(request.playerId)
              purchase <- marketService.buyVenue(player, venue)
            } yield purchase

          action match {
            case Left(err) => handleError(err)
            case Right(venue) =>
              complete(s"${venue.name} was bought by ${request.playerId} for ${venue.price}")
          }
        }
      }
    }

  private def getVenue(venueId: String): Either[ValidationError, Venue] =
    venueService.getVenue(venueId) match {
      case None        => Left(VenueNotFound(venueId))
      case Some(venue) => Right(venue)
    }

  private def getPlayer(playerId: String): Either[ValidationError, Player] =
    playerService.getPlayer(playerId) match {
      case None         => Left(PlayerNotFound(playerId))
      case Some(player) => Right(player)
    }

  private def handleError(err: ValidationError): StandardRoute =
    err match {
      case VenueAlreadyExistsError(id) => badRequest(s"Error: Venue with id '$id' already exists")
      case VenueNotFound(id)           => badRequest(s"Error: Venue with id '$id' not found")
      case PlayerNotFound(id)          => badRequest(s"Error: Player with id '$id' not found")
      case NotEnoughFunds(playerId, venueId)    => badRequest(s"$playerId can't afford $venueId")
      case VenueAlreadyOwned(playerId, venueId) => badRequest(s"$playerId already owns $venueId")
      case _ => complete(StatusCodes.InternalServerError, "Unknown error occured")
    }

  private def badRequest[T](
    s: => T
  )(
    implicit m: ToEntityMarshaller[T]
  ) = complete(StatusCodes.BadRequest, s)

  val venuesRoute =
    pathPrefix("venues") {
      pathEnd {
        getVenues
      } ~ concat(
        createVenue,
        deleteVenue,
        buyVenue,
      )
    }

}

object VenueController {

  def routes(
    venueService: VenueService,
    marketService: MarketService,
    playerService: PlayerService,
  ) = new VenueController(venueService, marketService, playerService).venuesRoute

}
