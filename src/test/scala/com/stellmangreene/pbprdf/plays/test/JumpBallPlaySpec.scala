package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.JumpBallPlay
import com.stellmangreene.pbprdf.test.TestIri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the JumpBallPlay class
 *
 * @author andrewstellman
 */
class JumpBallPlaySpec extends FlatSpec with Matchers {

  behavior of "JumpBallPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse jump ball triples" in {
    val testIri = TestIri.create("400610736")
    val play = new JumpBallPlay(testIri, 426, 5, "4:58", "Mercury", "Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)", "78-78", GamePeriodInfo.WNBAPeriodInfo)
    play.addRdf(rep)

    val statements = rep
      .executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610736/426> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#JumpBall",
        s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
        "http://stellman-greene.com/pbprdf#period -> 5",
        "http://stellman-greene.com/pbprdf#time -> 4:58",
        "http://stellman-greene.com/pbprdf#secondsIntoGame -> 2402",
        "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 298",
        "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mercury",
        "http://stellman-greene.com/pbprdf#jumpBallHomePlayer -> http://stellman-greene.com/pbprdf/players/Brittney_Griner",
        "http://stellman-greene.com/pbprdf#jumpBallAwayPlayer -> http://stellman-greene.com/pbprdf/players/Elena_Delle_Donne",
        "http://stellman-greene.com/pbprdf#jumpBallGainedPossession -> http://stellman-greene.com/pbprdf/players/DeWanna_Bonner",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mercury: Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)"))

    val testIri2 = TestIri.create("400610636")
    val play2 = new JumpBallPlay(testIri2, 1, 1, "10:00", "Sun", "Stefanie Dolson vs. Kelsey Bone", "0-0", GamePeriodInfo.WNBAPeriodInfo)
    play2.addRdf(rep)

    val statements2 = rep
      .executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610636/1> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements2 should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#JumpBall",
        s"http://stellman-greene.com/pbprdf#inGame -> ${testIri2.stringValue}",
        "http://stellman-greene.com/pbprdf#period -> 1",
        "http://stellman-greene.com/pbprdf#time -> 10:00",
        "http://stellman-greene.com/pbprdf#secondsIntoGame -> 0",
        "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 600",
        "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
        "http://stellman-greene.com/pbprdf#jumpBallHomePlayer -> http://stellman-greene.com/pbprdf/players/Kelsey_Bone",
        "http://stellman-greene.com/pbprdf#jumpBallAwayPlayer -> http://stellman-greene.com/pbprdf/players/Stefanie_Dolson",
        "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Stefanie Dolson vs. Kelsey Bone"))
  }

}
