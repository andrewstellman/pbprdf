package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.TurnoverPlay
import com.stellmangreene.pbprdf.test.TestUri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the TurnoverPlay class
 *
 * @author andrewstellman
 */
class TurnoverPlaySpec extends FlatSpec with Matchers {

  behavior of "TurnoverPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  val testUri = TestUri.create("400610636")

  it should "parse a turnover" in {
    new TurnoverPlay(testUri, 167, 1, "1:05", "Mystics", "Kayla Thornton turnover", "40-38", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/167> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 65",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 535",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> turnover",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton turnover"))
  }

  it should "parse a lost ball turnover" in {
    new TurnoverPlay(testUri, 17, 1, "8:00", "Sun", "Tierra Ruffin-Pratt lost ball turnover (Alex Bentley steals)", "5-0", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/17> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 8:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 120",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 480",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> lost ball turnover",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Tierra_Ruffin-Pratt",
          "http://www.stellman-greene.com/pbprdf#stolenBy -> http://www.stellman-greene.com/pbprdf/players/Alex_Bentley",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Tierra Ruffin-Pratt lost ball turnover (Alex Bentley steals)"))
  }

  it should "parse a shot clock violation" in {
    new TurnoverPlay(testUri, 84, 1, "9:36", "Sun", "shot clock turnover", "18-24", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/84> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 9:36",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 24",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 576",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> shot clock",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: shot clock turnover"))
  }

  it should "parse a bad pass" in {
    new TurnoverPlay(testUri, 195, 2, "6:54", "Sun", "Alex Bentley bad pass", "52-40", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/195> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 6:54",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 786",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 414",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> bad pass",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Alex_Bentley",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Alex Bentley bad pass"))
  }

  it should "parse a bad pass and steal" in {
    new TurnoverPlay(testUri, 366, 4, "8:04", "Mystics", "Ivory Latta bad pass (Kelsey Bone steals)", "69-66", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/366> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 4",
          "http://www.stellman-greene.com/pbprdf#time -> 8:04",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1916",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 484",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> bad pass",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Ivory_Latta",
          "http://www.stellman-greene.com/pbprdf#stolenBy -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Ivory Latta bad pass (Kelsey Bone steals)"))
  }

  it should "parse a traveling violation" in {
    new TurnoverPlay(testUri, 204, 2, "1:09", "Sun", "Kelsey Bone traveling", "52-42", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/204> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1131",
          "http://www.stellman-greene.com/pbprdf#time -> 1:09",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 69",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> traveling",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelsey Bone traveling"))
  }

  it should "parse a kicked ball violation" in {
    new TurnoverPlay(testUri, 337, 3, "4:16", "Sun", "Kara Lawson kicked ball violation", "63-61", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/337> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 3",
          "http://www.stellman-greene.com/pbprdf#time -> 4:16",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1544",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 256",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> kicked ball violation",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Kara_Lawson",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kara Lawson kicked ball violation"))
  }

  it should "parse an out-of-bounds bad pass turnover" in {
    val testUri2 = TestUri.create("400610636")
    new TurnoverPlay(testUri2, 345, 4, "2:38", "Sparks", "Candace Parker Out-of-Bounds Bad Pass Turnover", "67-70", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/345> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 4",
          "http://www.stellman-greene.com/pbprdf#time -> 2:38",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 2242",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 158",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sparks",
          "http://www.stellman-greene.com/pbprdf#turnoverType -> out-of-bounds bad pass",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> http://www.stellman-greene.com/pbprdf/players/Candace_Parker",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Candace Parker Out-of-Bounds Bad Pass Turnover"))

  }
}
