package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.plays.ThreeSecondViolationPlay
import com.stellmangreene.pbprdf.util.RdfOperations

/**
 * Test the ThreeSecondViolationPlay class
 *
 * @author andrewstellman
 */
class ThreeSecondViolationPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "ThreeSecondViolationPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a three-second violation" in {
    new ThreeSecondViolationPlay("400610636", 146, 1, "4:07", "Sun", "Kara Lawson offensive 3-seconds (Technical Foul)", "33-31").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/146> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#TechnicalFoul",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 4:07",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 353",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Kara Lawson",
          "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
          "http://www.stellman-greene.com/pbprdf#isThreeSecond -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kara Lawson offensive 3-seconds (Technical Foul)"))

  }

}
