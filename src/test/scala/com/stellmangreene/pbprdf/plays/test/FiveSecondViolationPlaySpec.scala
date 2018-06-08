package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.FiveSecondViolationPlay
import com.stellmangreene.pbprdf.test.TestIri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the FiveSecondViolationPlay class
 *
 * @author andrewstellman
 */
class FiveSecondViolationPlaySpec extends FlatSpec with Matchers {

  behavior of "FiveSecondViolationPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a five-second violation" in {

    val testIri = TestIri.create("400975630")
    new FiveSecondViolationPlay(testIri, 70, 1, "2:39", "Magic", "5 second violation", "18-19", GamePeriodInfo.NBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400975630/70> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#FiveSecondViolation",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 1",
          "http://stellman-greene.com/pbprdf#time -> 2:39",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 561",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 159",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Magic",
          "http://www.w3.org/2000/01/rdf-schema#label -> Magic: 5 second violation"))
  }

}
