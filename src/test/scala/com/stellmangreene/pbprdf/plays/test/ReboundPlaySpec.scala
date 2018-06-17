package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.ReboundPlay
import com.stellmangreene.pbprdf.test.TestIri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the ReboundPlay class
 *
 * @author andrewstellman
 */
class ReboundPlaySpec extends FlatSpec with Matchers {

  behavior of "ReboundPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse rebound triples" in {
    val testIri = TestIri.create("400610636")
    val play = new ReboundPlay(testIri, 125, 2, "4:02", "Mystics", "Emma Meesseman offensive rebound", "31-30", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610636/125> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Rebound",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
        "http://stellman-greene.com/pbprdf#awayScore -> 31",
        "http://stellman-greene.com/pbprdf#homeScore -> 30",
        s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
        "http://stellman-greene.com/pbprdf#period -> 2",
        "http://stellman-greene.com/pbprdf#time -> 4:02",
        "http://stellman-greene.com/pbprdf#secondsIntoGame -> 958",
        "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 242",
        "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mystics",
        "http://stellman-greene.com/pbprdf#reboundedBy -> http://stellman-greene.com/pbprdf/players/Emma_Meesseman",
        "http://stellman-greene.com/pbprdf#isOffensive -> true",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Emma Meesseman offensive rebound"))
  }

}
