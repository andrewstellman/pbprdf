package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.plays.EndOfPlay
import com.stellmangreene.pbprdf.test.TestIri
import com.stellmangreene.pbprdf.GamePeriodInfo

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the TurnoverPlay class
 *
 * @author andrewstellman
 */
class EndOfPlaySpec extends FlatSpec with Matchers {

  behavior of "EndOfPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse an official timeout" in {
    val testIri = TestIri.create("400610636")
    val play = new EndOfPlay(testIri, 125, 2, "0.0", "Mystics", "End of the 2nd Quarter", "44-40", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610636/125> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#EndOfPeriod",
        s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
        "http://stellman-greene.com/pbprdf#period -> 2",
        "http://stellman-greene.com/pbprdf#time -> 0.0",
        "http://stellman-greene.com/pbprdf#secondsIntoGame -> 1200",
        "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 0",
        "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mystics",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: End of the 2nd Quarter"))

  }

  it should "parse a full timeout" in {
    val testIri = TestIri.create("400610636")
    val play = new EndOfPlay(testIri, 391, 4, "0.0", "Mystics", "End of Game", "73-68", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610636/391> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#EndOfGame",
        s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
        "http://stellman-greene.com/pbprdf#period -> 4",
        "http://stellman-greene.com/pbprdf#time -> 0.0",
        "http://stellman-greene.com/pbprdf#secondsIntoGame -> 2400",
        "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 0",
        "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mystics",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: End of Game"))

  }
}
