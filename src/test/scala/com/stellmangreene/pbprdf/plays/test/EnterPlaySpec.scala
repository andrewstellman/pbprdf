package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.test.TestIri
import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.EnterPlay

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the EnterPlay class
 *
 * @author andrewstellman
 */
class EnterPlaySpec extends FlatSpec with Matchers {

  behavior of "EnterPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse enter triples" in {

    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    var testIri = TestIri.create("400610636")
    new EnterPlay(testIri, 101, 1, "8:00", "Sun", "Kelly Faris enters the game for Alyssa Thomas", "21-26", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400610636/101> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Enters",
          "http://stellman-greene.com/pbprdf#awayScore -> 21",
          "http://stellman-greene.com/pbprdf#homeScore -> 26",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 1",
          "http://stellman-greene.com/pbprdf#time -> 8:00",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 120",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 480",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Sun",
          "http://stellman-greene.com/pbprdf#playerEntering -> http://stellman-greene.com/pbprdf/players/Kelly_Faris",
          "http://stellman-greene.com/pbprdf#playerExiting -> http://stellman-greene.com/pbprdf/players/Alyssa_Thomas",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelly Faris enters the game for Alyssa Thomas"))
  }

}
