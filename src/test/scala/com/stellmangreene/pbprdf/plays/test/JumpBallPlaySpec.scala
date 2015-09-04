package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.stellmangreene.pbprdf.plays.JumpBallPlay
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.test.TestUri

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
    val play = new JumpBallPlay(TestUri.create("400610736"), 426, 5, "4:58", "Mercury", "Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)", "78-78")
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
        "http://www.stellman-greene.com/pbprdf#team -> http://www.stellman-greene.com/pbprdf/teams/Mercury",
        "http://www.stellman-greene.com/pbprdf#jumpBallHomePlayer -> http://www.stellman-greene.com/pbprdf/players/Brittney_Griner",
        "http://www.stellman-greene.com/pbprdf#jumpBallAwayPlayer -> http://www.stellman-greene.com/pbprdf/players/Elena_Delle_Donne",
        "http://www.stellman-greene.com/pbprdf#jumpBallGainedPossession -> http://www.stellman-greene.com/pbprdf/players/DeWanna_Bonner",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mercury: Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)"))

    val play2 = new JumpBallPlay(TestUri.create("400610636"), 1, 1, "10:00", "Sun", "Stefanie Dolson vs. Kelsey Bone", "0-0")
    play2.addRdf(rep)

    val statements2 = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/1> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements2 should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#JumpBall",
        "http://www.stellman-greene.com/pbprdf#period -> 1",
        "http://www.stellman-greene.com/pbprdf#time -> 10:00",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 0",
        "http://www.stellman-greene.com/pbprdf#team -> http://www.stellman-greene.com/pbprdf/teams/Sun",
        "http://www.stellman-greene.com/pbprdf#jumpBallHomePlayer -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
        "http://www.stellman-greene.com/pbprdf#jumpBallAwayPlayer -> http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson",
        "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Stefanie Dolson vs. Kelsey Bone"))
  }

}
