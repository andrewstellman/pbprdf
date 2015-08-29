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

/**
 * Test the EspnPlayByPlay class
 * @author andrewstellman
 */
class EspnPlayByPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "an instance of EspnPlayByPlay"

  val xmlStream = new FileInputStream("src/test/resources/com/stellmangreene/pbprdf/test/htmldata/400610636.html")
  val rootElem = XmlHelper.parseXml(xmlStream)
  var playByPlay = new EspnPlayByPlay("400610636", rootElem)

  it should "read information about the game" in {
    playByPlay.homeTeam should be(Some("Sun"))
    playByPlay.homeScore should be(Some("68"))
    playByPlay.awayTeam should be(Some("Mystics"))
    playByPlay.awayScore should be(Some("73"))
    playByPlay.gameLocation should be(Some("Mohegan Sun Arena, Uncasville, CT"))
    playByPlay.gameTime should be(Some("7:00 PM ET, June 5, 2015"))
    playByPlay.toString should be("Mystics at Sun on 7:00 PM ET, June 5, 2015: 391 events")
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
    var emptyPlayByPlay = new EspnPlayByPlay("INVALID", rootElem)
    emptyPlayByPlay.homeTeam should be(None)
    emptyPlayByPlay.homeScore should be(None)
    emptyPlayByPlay.awayTeam should be(None)
    emptyPlayByPlay.awayScore should be(None)
    emptyPlayByPlay.gameLocation should be(None)
    emptyPlayByPlay.gameTime should be(None)
    emptyPlayByPlay.events should be(Seq())
    emptyPlayByPlay.toString should be("(away team name not found) at (home team name not found) on (game time not found): 0 events")
  }

  it should "generate RDF" in {

    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    playByPlay.addRdf(rep)

    rep.executeQuery("SELECT * { ?s ?p ?o }").toIterator().size should be(4103)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/1> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#JumpBall",
          "http://www.stellman-greene.com/pbprdf#time -> 10:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 0",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#jumpBallAwayPlayer -> Stefanie Dolson",
          "http://www.stellman-greene.com/pbprdf#jumpBallHomePlayer -> Kelsey Bone",
          "http://www.stellman-greene.com/pbprdf#jumpBallGainedPossession -> Jasmine Thomas",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/166> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1135",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Kayla Thornton",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> Jasmine Thomas",
          "http://www.stellman-greene.com/pbprdf#isCharge -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)"))

    rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/119> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Timeout",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 7:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 775",
          "http://www.stellman-greene.com/pbprdf#team -> Connecticut",
          "http://www.stellman-greene.com/pbprdf#timeoutDuration -> Full",
          "http://www.w3.org/2000/01/rdf-schema#label -> Connecticut Full timeout"))

  }

}