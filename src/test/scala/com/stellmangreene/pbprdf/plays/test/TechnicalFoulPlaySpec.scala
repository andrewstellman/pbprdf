package com.stellmangreene.pbprdf.plays.test

import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.DoubleTechnicalFoulPlay
import com.stellmangreene.pbprdf.plays.TechnicalFoulPlay
import com.stellmangreene.pbprdf.test.TestIri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the TechnicalFoulPlay class
 *
 * @author andrewstellman
 */
class TechnicalFoulPlaySpec extends FlatSpec with Matchers {

  behavior of "TechnicalFoulPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse a technical foul" in {
    val testIri = TestIri.create("400496779")
    new TechnicalFoulPlay(testIri, 93, 2, "7:37", "Mercury", "Diana Taurasi technical foul(1st technical foul)", "21-28", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400496779/93> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 2",
          "http://stellman-greene.com/pbprdf#time -> 7:37",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 743",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 457",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mercury",
          "http://stellman-greene.com/pbprdf#foulCommittedBy -> http://stellman-greene.com/pbprdf/players/Diana_Taurasi",
          "http://stellman-greene.com/pbprdf#technicalFoulNumber -> 1",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mercury: Diana Taurasi technical foul(1st technical foul)"))

  }

  it should "parse a technical foul with no player specified" in {
    val testIri = TestIri.create("400496779")
    new TechnicalFoulPlay(testIri, 152, 2, "1:03", "Mercury", "technical foul(2nd technical foul)", "37-32", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400496779/152> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 2",
          "http://stellman-greene.com/pbprdf#time -> 1:03",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 1137",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 63",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Mercury",
          "http://stellman-greene.com/pbprdf#technicalFoulNumber -> 2",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mercury: technical foul(2nd technical foul)"))

  }

  it should "parse a double technical foul" in {
    val testIri = TestIri.create("400445797")
    new DoubleTechnicalFoulPlay(testIri, 231, 3, "4:22", "Shock", "Double technical foul: Marissa Coleman and Glory Johnson", "47-41", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://stellman-greene.com/pbprdf/400445797/231> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://stellman-greene.com/pbprdf#TechnicalFoul",
          s"http://stellman-greene.com/pbprdf#inGame -> ${testIri.stringValue}",
          "http://stellman-greene.com/pbprdf#period -> 3",
          "http://stellman-greene.com/pbprdf#time -> 4:22",
          "http://stellman-greene.com/pbprdf#secondsIntoGame -> 1538",
          "http://stellman-greene.com/pbprdf#secondsLeftInPeriod -> 262",
          "http://stellman-greene.com/pbprdf#foulCommittedBy -> http://stellman-greene.com/pbprdf/players/Marissa_Coleman",
          "http://stellman-greene.com/pbprdf#foulCommittedBy -> http://stellman-greene.com/pbprdf/players/Glory_Johnson",
          "http://stellman-greene.com/pbprdf#forTeam -> http://stellman-greene.com/pbprdf/teams/Shock",
          "http://www.w3.org/2000/01/rdf-schema#label -> Shock: Double technical foul: Marissa Coleman and Glory Johnson"))
  }

}
