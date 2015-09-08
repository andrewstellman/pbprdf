package com.stellmangreene.pbprdf.test

import java.io.FileInputStream
import org.scalatest._
import com.stellmangreene.pbprdf.EspnPlayByPlay
import com.stellmangreene.pbprdf.util.XmlHelper
import com.stellmangreene.pbprdf.plays.Play
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.RepositoryResult
import info.aduna.iteration.Iteration
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.plays.test.EnterPlaySpec
import org.joda.time.DateTime
import com.stellmangreene.pbprdf.InvalidPlayByPlayException

/**
 * Test the EspnPlayByPlay class
 * @author andrewstellman
 */
class EspnPlayByPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "EspnPlayByPlay"

  val xmlStream = new FileInputStream("src/test/resources/com/stellmangreene/pbprdf/test/htmldata/400610636.html")
  val rootElem = XmlHelper.parseXml(xmlStream)
  var playByPlay = new EspnPlayByPlay(rootElem, "400610636.html")

  it should "read information about the game" in {
    playByPlay.homeTeam should be("Sun")
    playByPlay.homeScore should be("68")
    playByPlay.awayTeam should be("Mystics")
    playByPlay.awayScore should be("73")
    playByPlay.gameLocation should be("Mohegan Sun Arena, Uncasville, CT")
    playByPlay.gameTime should equal(new DateTime("2015-06-05T19:00:00.000-05:00"))
    playByPlay.toString should be("Mystics (73) at Sun (68) on 2015-06-05: 391 events")
  }

  it should "read the events from the game" in {
    playByPlay.events.size should be(391)

    playByPlay.events.filter(_.period == 1)
      .filter(!_.isInstanceOf[Play])
      .map(_.description) should be(List("Official timeout", "End of the 1st Quarter"))

    val firstQuarterPlays = playByPlay.events
      .filter(_.period == 1)
      .filter(_.isInstanceOf[Play])
    firstQuarterPlays.size should be(81)
    firstQuarterPlays
      .filter(_.description.contains("Stefanie Dolson makes"))
      .map(_.description) should be(
        List(
          "Mystics: Stefanie Dolson makes layup (Emma Meesseman assists)",
          "Mystics: Stefanie Dolson makes layup (Natasha Cloud assists)",
          "Mystics: Stefanie Dolson makes layup"))
  }

  it should "read an invalid XML file" in {
    val xmlStream = new FileInputStream("src/main/resources/logback.xml")
    val rootElem = XmlHelper.parseXml(xmlStream)
    intercept[InvalidPlayByPlayException] {
      var invalidPlayByPlay = new EspnPlayByPlay(rootElem, "INVALID FILE")
    }
  }

  it should "generate RDF" in {

    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    playByPlay.addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .filter(!_.contains("node"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Game",
          "http://www.stellman-greene.com/pbprdf#gameTime -> 2015-06-05T19:00:00.000-05:00",
          "http://www.stellman-greene.com/pbprdf#homeTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#awayTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#gameLocation -> Mohegan Sun Arena, Uncasville, CT",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics (73) at Sun (68) on 2015-06-05: 391 events"))

    rep.executeQuery("""
BASE <http://www.stellman-greene.com>
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>

SELECT * {  
   ?s pbprdf:hasAwayTeamRoster ?roster .
   ?roster ?p ?o .
}
""")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Roster",
          "http://www.stellman-greene.com/pbprdf#rosterTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Emma_Meesseman",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Ivory_Latta",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Armintie_Herrington",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Tierra_Ruffin-Pratt",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Natasha_Cloud",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Tayler_Hill",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Kara_Lawson",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Ally_Malott",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics"))

    rep.executeQuery("""
BASE <http://www.stellman-greene.com>
PREFIX pbprdf: <http://www.stellman-greene.com/pbprdf#>

SELECT * {  
   ?s pbprdf:hasHomeTeamRoster ?roster .
   ?roster ?p ?o .
}
""")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Roster",
          "http://www.stellman-greene.com/pbprdf#rosterTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Chelsea_Gray",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Kelly_Faris",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Alyssa_Thomas",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Elizabeth_Williams",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Kayla_Pedersen",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Alex_Bentley",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Shekinna_Stricklen",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://www.stellman-greene.com/pbprdf#hasPlayer -> http://www.stellman-greene.com/pbprdf/players/Camille_Little",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set("http://www.w3.org/2000/01/rdf-schema#label -> Stefanie Dolson"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/1> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#JumpBall",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 10:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 0",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 600",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#jumpBallHomePlayer -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://www.stellman-greene.com/pbprdf#jumpBallAwayPlayer -> http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://www.stellman-greene.com/pbprdf#jumpBallGainedPossession -> http://www.stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/166> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1135",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 65",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> http://www.stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://www.stellman-greene.com/pbprdf#isCharge -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton offensive Charge (Jasmine Thomas draws the foul)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/games/2015-06-05_Mystics_at_Sun/119> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Timeout",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 7:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 775",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 425",
          "http://www.stellman-greene.com/pbprdf#timeoutDuration -> Full",
          "http://www.stellman-greene.com/pbprdf#timeoutTeam -> Connecticut",
          "http://www.w3.org/2000/01/rdf-schema#label -> Connecticut Full timeout"))

  }

}