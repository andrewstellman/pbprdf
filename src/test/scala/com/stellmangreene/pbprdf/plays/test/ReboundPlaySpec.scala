package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.stellmangreene.pbprdf.plays.ReboundPlay
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.test.TestUri

/**
 * Test the ReboundPlay class
 *
 * @author andrewstellman
 */
class ReboundPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "ReboundPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse rebound triples" in {
    val play = new ReboundPlay(TestUri.create("400610636"), 125, 2, "4:02", "Mystics", "Emma Meesseman offensive rebound", "31-30")
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/125> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Rebound",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.stellman-greene.com/pbprdf#period -> 2",
        "http://www.stellman-greene.com/pbprdf#time -> 4:02",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 958",
        "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 242",
        "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
        "http://www.stellman-greene.com/pbprdf#reboundedBy -> http://www.stellman-greene.com/pbprdf/players/Emma_Meesseman",
        "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Emma Meesseman offensive rebound"))
  }

}
