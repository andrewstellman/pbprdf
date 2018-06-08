package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.EjectionPlay
import com.stellmangreene.pbprdf.test.TestIri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the EjectionPlay class
 *
 * @author andrewstellman
 */
class EjectionPlaySpec extends FlatSpec with Matchers {

  behavior of "EjectionPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a five-second violation" in {

    val testIri = TestIri.create("400981090")
    new EjectionPlay(testIri, 367, 4, "1:10", "Wings", "Aerial Powers ejected", "71-76", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400981090/367> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Ejection",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 4",
          "http://stellman-greene.com/pbprdf#time -> 1:10",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 2330",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 70",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Wings",
          "http://stellman-greene.com/pbprdf#playerEjected -> http://stellman-greene.com/pbprdf/players/Aerial_Powers",
          "http://www.w3.org/2000/01/rdf-schema#label -> Wings: Aerial Powers ejected"))
  }

}
