package com.stellmangreene.pbprdf.test

import org.joda.time.DateTime
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.EspnPlayByPlay
import com.stellmangreene.pbprdf.InvalidPlayByPlayException
import com.stellmangreene.pbprdf.plays.Play

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the EspnPlayByPlay class
 * @author andrewstellman
 */
class EspnPlayByPlaySpec extends FlatSpec with Matchers {

  val path = "src/test/resources/com/stellmangreene/pbprdf/test/htmldata/"

  behavior of "EspnPlayByPlay"

  var wnbaPlayByPlay = new EspnPlayByPlay(path, "400610636.html", "400610636-gameinfo.html")
  var nbaPlayByPlay = new EspnPlayByPlay(path, "401029417.html", "401029417-gameinfo.html")

  it should "read information about a WNBA game" in {
    wnbaPlayByPlay.homeTeam should be("Sun")
    wnbaPlayByPlay.homeScore should be("68")
    wnbaPlayByPlay.awayTeam should be("Mystics")
    wnbaPlayByPlay.awayScore should be("73")
    wnbaPlayByPlay.gameLocation should be(Some("Mohegan Sun Arena"))
    wnbaPlayByPlay.gameTime should equal(new DateTime("2015-06-05T19:00:00.000-04:00"))
    wnbaPlayByPlay.toString should be("Mystics (73) at Sun (68) on 2015-06-05: 391 events")
  }

  it should "read information about an NBA game" in {
    nbaPlayByPlay.homeTeam should be("Cavaliers")
    nbaPlayByPlay.homeScore should be("80")
    nbaPlayByPlay.awayTeam should be("Pacers")
    nbaPlayByPlay.awayScore should be("98")
    nbaPlayByPlay.gameLocation should be(Some("Quicken Loans Arena"))
    nbaPlayByPlay.gameTime should equal(new DateTime("2018-04-15T14:30:00.000-05:00"))
    nbaPlayByPlay.toString should be("Pacers (98) at Cavaliers (80) on 2018-04-15: 458 events")
  }

  it should "read the events from the game" in {
    wnbaPlayByPlay.events.size should be(391)

    wnbaPlayByPlay.events.filter(_.period == 1)
      .filter(!_.isInstanceOf[Play])
      .map(_.description) should be(List())

    val firstQuarterPlays = wnbaPlayByPlay.events
      .filter(_.period == 1)
      .filter(_.isInstanceOf[Play])
    firstQuarterPlays.size should be(83)
    firstQuarterPlays
      .filter(_.description.contains("Stefanie Dolson makes"))
      .map(_.description) should be(
        List(
          "Mystics: Stefanie Dolson makes layup (Emma Meesseman assists)",
          "Mystics: Stefanie Dolson makes layup (Natasha Cloud assists)",
          "Mystics: Stefanie Dolson makes layup"))
  }

  it should "read an invalid XML file" in {
    intercept[InvalidPlayByPlayException] {
      var invalidPlayByPlay = new EspnPlayByPlay(path, "400610636.html", "INVALID FILENAME")
    }
    intercept[InvalidPlayByPlayException] {
      var xmlFile = new EspnPlayByPlay("src/main/resources", "logback.xml", "logback.xml")
    }
  }

  it should "generate RDF" in {

    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    wnbaPlayByPlay.addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .filter(!_.contains("node"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Game",
          "http://stellman-greene.com/pbprdf#gameTime -> 2015-06-05T18:00:00.000-05:00",
          "http://stellman-greene.com/pbprdf#homeTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
          "http://stellman-greene.com/pbprdf#awayTeam -> http://stellman-greene.com/pbprdf/teams/Mystics",
          "http://stellman-greene.com/pbprdf#gameLocation -> Mohegan Sun Arena",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics (73) at Sun (68) on 2015-06-05: 391 events"))

    rep.executeQuery("""
BASE <http://stellman-greene.com>
PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>

SELECT * {  
   ?s pbprdf:hasAwayTeamRoster ?roster .
   ?roster ?p ?o .
}
""")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Roster",
          "http://stellman-greene.com/pbprdf#rosterTeam -> http://stellman-greene.com/pbprdf/teams/Mystics",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Emma_Meesseman",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Ivory_Latta",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Armintie_Herrington",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Tierra_Ruffin-Pratt",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Natasha_Cloud",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Tayler_Hill",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Kara_Lawson",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Ally_Malott",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics"))

    rep.executeQuery("""
BASE <http://stellman-greene.com>
PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>

SELECT * {  
   ?s pbprdf:hasHomeTeamRoster ?roster .
   ?roster ?p ?o .
}
""")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Roster",
          "http://stellman-greene.com/pbprdf#rosterTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Chelsea_Gray",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Kelly_Faris",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Alyssa_Thomas",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Elizabeth_Williams",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Kayla_Pedersen",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Alex_Bentley",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Shekinna_Stricklen",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://stellman-greene.com/pbprdf#hasPlayer -> http://stellman-greene.com/pbprdf/players/Camille_Little",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun"))

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/players/Stefanie_Dolson> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Player",
          "http://www.w3.org/2000/01/rdf-schema#label -> Stefanie Dolson"))

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/1> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://stellman-greene.com/pbprdf#nextEvent -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/2",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#JumpBall",
          "http://stellman-greene.com/pbprdf#inGame -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun",
          "http://stellman-greene.com/pbprdf#period -> 1",
          "http://stellman-greene.com/pbprdf#time -> 10:00",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 0",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 600",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
          "http://stellman-greene.com/pbprdf#jumpBallHomePlayer -> http://stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://stellman-greene.com/pbprdf#jumpBallAwayPlayer -> http://stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://stellman-greene.com/pbprdf#jumpBallGainedPossession -> http://stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)"))

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/166> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://stellman-greene.com/pbprdf#previousEvent -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/165",
          "http://stellman-greene.com/pbprdf#nextEvent -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/167",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Foul",
          "http://stellman-greene.com/pbprdf#inGame -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun",
          "http://stellman-greene.com/pbprdf#period -> 2",
          "http://stellman-greene.com/pbprdf#time -> 1:05",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 1135",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 65",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mystics",
          "http://stellman-greene.com/pbprdf#foulCommittedBy -> http://stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://stellman-greene.com/pbprdf#foulDrawnBy -> http://stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://stellman-greene.com/pbprdf#isCharge -> true",
          "http://stellman-greene.com/pbprdf#isOffensive -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton offensive Charge (Jasmine Thomas draws the foul)"))

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/119> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://stellman-greene.com/pbprdf#previousEvent -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/118",
          "http://stellman-greene.com/pbprdf#nextEvent -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/120",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Timeout",
          "http://stellman-greene.com/pbprdf#inGame -> http://stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun",
          "http://stellman-greene.com/pbprdf#period -> 2",
          "http://stellman-greene.com/pbprdf#time -> 7:05",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 775",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 425",
          "http://stellman-greene.com/pbprdf#timeoutDuration -> Full",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Connecticut Full timeout"))

  }

}