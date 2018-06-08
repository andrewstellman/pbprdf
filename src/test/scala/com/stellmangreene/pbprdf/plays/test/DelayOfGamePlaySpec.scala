package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.plays.DelayOfGamePlay
import com.stellmangreene.pbprdf.test.TestIri
import com.stellmangreene.pbprdf.GamePeriodInfo

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the DelayOfGamePlay class
 *
 * @author andrewstellman
 */
class DelayOfGamePlaySpec extends FlatSpec with Matchers {

  behavior of "DelayOfGamePlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a delay of game violation" in {
    val testIri = TestIri.create("400610739")

    new DelayOfGamePlay(testIri, 86, 2, "10:00", "Sparks", "Los Angeles delay of game violation", "15-22", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new DelayOfGamePlay(testIri, 295, 3, "1:39", "Sparks", "delay techfoul", "54-56", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610739/86> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 2",
          "http://stellman-greene.com/pbprdf#time -> 10:00",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 600",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 600",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sparks",
          "http://stellman-greene.com/pbprdf#isDelayOfGame -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Los Angeles delay of game violation"))

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610739/295> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 3",
          "http://stellman-greene.com/pbprdf#time -> 1:39",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 1701",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 99",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sparks",
          "http://stellman-greene.com/pbprdf#isDelayOfGame -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: delay techfoul"))
  }

}
