package io.reality

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.reality.bootstrap.Bootstrap
import io.reality.controller.VenueController
import io.reality.domain.Venue
import io.reality.repository.inmemory._
import io.reality.service._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.concurrent.TrieMap

class VenueSpec extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  val venueRepository  = VenueRepository.make(new TrieMap[String, Venue]())
  val playerRepository = PlayerRepository.make(Bootstrap.playerData)

  val playerService = PlayerService.make(playerRepository)
  val venueService  = VenueService.make(venueRepository)
  val marketService = MarketService.make(venueService, playerService)

  val fullRoute = VenueController.routes(venueService, marketService, playerService)

  // Data

  val venue1Json  = """{"name":"First Venue","price":100}"""
  val venue2Json  = """{"name":"Second Venue","price":50}"""
  val venue3Json  = """{"name":"Third Venue","price":1000}"""
  val player1Json = """{"playerId":"player1"}"""

  val venue1 = HttpEntity(
    ContentTypes.`application/json`,
    venue1Json,
  )

  val venue2 = HttpEntity(
    ContentTypes.`application/json`,
    venue2Json,
  )

  val venue3 = HttpEntity(
    ContentTypes.`application/json`,
    venue3Json,
  )

  val player1 = HttpEntity(
    ContentTypes.`application/json`,
    player1Json,
  )

  //

  "GET" should "return empty array when there are no venues" in {
    Get("/venues") ~> fullRoute ~> check {
      responseAs[String] shouldEqual "[]"
    }
  }

  "GET" should "return array of two venues" in {
    Put("/venues/123", venue1) ~> fullRoute
    Put("/venues/1234", venue2) ~> fullRoute

    val expectedVenue1 = venue1Json.replace("{", """{"id":"123",""")
    val expectedVenue2 = venue2Json.replace("{", """{"id":"1234",""")

    Get("/venues") ~> fullRoute ~> check {
      responseAs[String] shouldEqual s"[$expectedVenue1,$expectedVenue2]"
    }
  }

  "PUT" should "return venue id of newly created venue" in {
    Put("/venues/1", venue1) ~> fullRoute ~> check {
      responseAs[String] shouldEqual "1"
      response.status shouldEqual StatusCodes.Created
    }
  }

  "PUT" should "return an error if venue with given id already exists" in {
    Put("/venues/1", venue1) ~> fullRoute

    Put("/venues/1", venue1) ~> fullRoute ~> check {
      responseAs[String] shouldEqual "Error: Venue with id '1' already exists"
      response.status shouldEqual StatusCodes.BadRequest
    }
  }

  "DELETE" should "remove existing venue" in {
    Put("/venues/12", venue1) ~> fullRoute

    Delete("/venues/12") ~> fullRoute ~> check {
      responseAs[String] shouldEqual "12"
      response.status shouldEqual StatusCodes.OK
    }
  }

  "DELETE" should "return error if venue does not exist" in {
    Delete("/venues/1234567") ~> fullRoute ~> check {
      responseAs[String] shouldEqual "Error: Venue with id '1234567' not found"
      response.status shouldEqual StatusCodes.BadRequest
    }
  }

  "POST {id}/buy" should "assign property with given id to player" in {
    Put("/venues/321", venue1) ~> fullRoute

    Post("/venues/321/buy", player1) ~> fullRoute ~> check {
      responseAs[String] shouldEqual "First Venue was bought by player1 for 100"
      response.status shouldEqual StatusCodes.OK
    }

    Get("/venues") ~> fullRoute ~> check {
      responseAs[String] should include(
        """{"id":"321","name":"First Venue","owner":"player1","price":100}"""
      )
    }
  }

  "POST {id}/buy" should "not change ownership of a venue if player has no sufficient funds" in {
    Put("/venues/453", venue3) ~> fullRoute

    Post("/venues/453/buy", player1) ~> fullRoute ~> check {
      responseAs[String] shouldEqual "player1 can't afford Third Venue"
      response.status shouldEqual StatusCodes.BadRequest
    }

    Get("/venues") ~> fullRoute ~> check {
      responseAs[String] should include(venue3Json.replace("{", """{"id":"453","""))
    }
  }

}
