import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.reality.bootstrap.Bootstrap
import io.reality.controller.VenueController
import io.reality.domain._
import io.reality.repository.inmemory.PlayerRepository
import io.reality.repository.inmemory.VenueRepository
import io.reality.service.MarketService
import io.reality.service.PlayerService
import io.reality.service.VenueService
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.collection.concurrent.TrieMap
import scala.io.StdIn

object Server extends App {

  implicit val system = ActorSystem(Behaviors.empty, "Reality-AS")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  val log = LoggerFactory.getLogger(this.getClass().getName())

  log.debug("Starting web server")

  val bindingFuture = Http()
    .newServerAt("localhost", 8080)
    .bind(prepareVenueController)

  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind())                 // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

  def prepareVenueController: Route = {
    val venueRepository  = VenueRepository.make(new TrieMap[String, Venue]())
    val playerRepository = PlayerRepository.make(Bootstrap.playerData)

    val playerService = PlayerService.make(playerRepository)
    val venueService  = VenueService.make(venueRepository)
    val marketService = MarketService.make(venueService, playerService)

    VenueController.routes(venueService, marketService, playerService)
  }

}
