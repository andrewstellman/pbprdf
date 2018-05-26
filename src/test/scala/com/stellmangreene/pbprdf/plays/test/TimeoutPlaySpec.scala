package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.stellmangreene.pbprdf.plays.TimeoutPlay
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.test.TestUri
import com.stellmangreene.pbprdf.GamePeriodInfo

/**
 * Test the TimeoutPlay class
 *
 * @author andrewstellman
 */
class TimeoutPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "TimeoutPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse an official timeout" in {
    val testUri = TestUri.create("400610636")
    val play = new TimeoutPlay(testUri, 125, 2, "4:56", "Mystics", "Official timeout", "10-9", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/125> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Timeout",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
        "http://www.stellman-greene.com/pbprdf#period -> 2",
        "http://www.stellman-greene.com/pbprdf#time -> 4:56",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 904",
        "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 296",
        "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
        "http://www.stellman-greene.com/pbprdf#isOfficial -> true",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Official timeout"))
  }

  it should "parse a full timeout" in {
    val testUri = TestUri.create("400610636")
    val play = new TimeoutPlay(testUri, 327, 2, "7:05", "Sun", "Connecticut Full timeout", "25-26", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/327> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Timeout",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
        "http://www.stellman-greene.com/pbprdf#period -> 2",
        "http://www.stellman-greene.com/pbprdf#time -> 7:05",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 775",
        "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 425",
        "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
        "http://www.stellman-greene.com/pbprdf#timeoutDuration -> Full",
        "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Connecticut Full timeout"))
  }
}
