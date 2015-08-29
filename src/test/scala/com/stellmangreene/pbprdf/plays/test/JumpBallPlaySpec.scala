package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.plays.JumpBallPlay
import com.stellmangreene.pbprdf.util.RdfOperations

/**
 * Test the JumpBallPlay class
 *
 * @author andrewstellman
 */
class JumpBallPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "JumpBallPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse jump ball triples" in {
    val play = new JumpBallPlay("400610736", 426, 5, "4:58", "Mercury", "Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)", "78-78")
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610736/426> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#JumpBall",
        "http://www.stellman-greene.com/pbprdf#period -> 5",
        "http://www.stellman-greene.com/pbprdf#time -> 4:58",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 2402",
        "http://www.stellman-greene.com/pbprdf#team -> Mercury",
        "http://www.stellman-greene.com/pbprdf#jumpBallHomePlayer -> Brittney Griner",
        "http://www.stellman-greene.com/pbprdf#jumpBallAwayPlayer -> Elena Delle Donne",
        "http://www.stellman-greene.com/pbprdf#jumpBallGainedPossession -> DeWanna Bonner",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mercury: Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)"))
  }

}
