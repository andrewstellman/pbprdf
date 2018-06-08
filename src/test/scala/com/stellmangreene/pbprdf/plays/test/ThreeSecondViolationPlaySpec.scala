package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.ThreeSecondViolationPlay
import com.stellmangreene.pbprdf.test.TestUri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the ThreeSecondViolationPlay class
 *
 * @author andrewstellman
 */
class ThreeSecondViolationPlaySpec extends FlatSpec with Matchers {

  behavior of "ThreeSecondViolationPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a three-second violation" in {
    val testUri = TestUri.create("400610636")
    new ThreeSecondViolationPlay(testUri, 146, 1, "4:07", "Sun", "Kara Lawson defensive 3-seconds (Technical Foul)", "33-31", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610636/146> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 1",
          "http://stellman-greene.com/pbprdf#time -> 4:07",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 353",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 247",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
          "http://stellman-greene.com/pbprdf#foulCommittedBy -> http://stellman-greene.com/pbprdf/players/Kara_Lawson",
          "http://stellman-greene.com/pbprdf#isThreeSecond -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kara Lawson defensive 3-seconds (Technical Foul)"))

    val testUri2 = TestUri.create("401031640")
    new ThreeSecondViolationPlay(testUri2, 20, 1, "8:28", "Jazz", "Rudy Gobert defensive 3-seconds (technical foul)", "12-5", GamePeriodInfo.NBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/401031640/20> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testUri2.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 1",
          "http://stellman-greene.com/pbprdf#time -> 8:28",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 212",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 508",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Jazz",
          "http://stellman-greene.com/pbprdf#foulCommittedBy -> http://stellman-greene.com/pbprdf/players/Rudy_Gobert",
          "http://stellman-greene.com/pbprdf#isThreeSecond -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Jazz: Rudy Gobert defensive 3-seconds (technical foul)"))
  }

}
