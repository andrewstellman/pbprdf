package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.stellmangreene.pbprdf.plays.EndOfPlay
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.test.TestUri
import com.stellmangreene.pbprdf.GamePeriodInfo

/**
 * Test the TurnoverPlay class
 *
 * @author andrewstellman
 */
class EndOfPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "EndOfPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse an official timeout" in {
    val testUri = TestUri.create("400610636")
    val play = new EndOfPlay(testUri, 125, 2, "0.0", "Mystics", "End of the 2nd Quarter", "44-40", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/125> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#EndOfPeriod",
        s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
        "http://www.stellman-greene.com/pbprdf#period -> 2",
        "http://www.stellman-greene.com/pbprdf#time -> 0.0",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1200",
        "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 0",
        "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: End of the 2nd Quarter"))

  }

  it should "parse a full timeout" in {
    val testUri = TestUri.create("400610636")
    val play = new EndOfPlay(testUri, 391, 4, "0.0", "Mystics", "End of Game", "73-68", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/391> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#EndOfGame",
        s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
        "http://www.stellman-greene.com/pbprdf#period -> 4",
        "http://www.stellman-greene.com/pbprdf#time -> 0.0",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 2400",
        "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 0",
        "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: End of Game"))

  }
}
