package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.stellmangreene.pbprdf.plays.DelayOfGamePlay
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.test.TestUri

/**
 * Test the DelayOfGamePlay class
 *
 * @author andrewstellman
 */
class DelayOfGamePlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "DelayOfGamePlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a delay of game violation" in {
    new DelayOfGamePlay(TestUri.create("400610739"), 86, 2, "10:00", "Sparks", "Los Angeles delay of game violation", "15-22").addRdf(rep)
    new DelayOfGamePlay(TestUri.create("400610739"), 295, 3, "1:39", "Sparks", "delay techfoul", "54-56").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610739/86> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#TechnicalFoul",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 10:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 600",
          "http://www.stellman-greene.com/pbprdf#team -> http://www.stellman-greene.com/pbprdf/teams/Sparks",
          "http://www.stellman-greene.com/pbprdf#isDelayOfGame -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Los Angeles delay of game violation"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610739/295> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#TechnicalFoul",
          "http://www.stellman-greene.com/pbprdf#period -> 3",
          "http://www.stellman-greene.com/pbprdf#time -> 1:39",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1701",
          "http://www.stellman-greene.com/pbprdf#team -> http://www.stellman-greene.com/pbprdf/teams/Sparks",
          "http://www.stellman-greene.com/pbprdf#isDelayOfGame -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: delay techfoul"))
  }

}
