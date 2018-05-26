package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.stellmangreene.pbprdf.plays.ShotPlay
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.test.TestUri
import com.stellmangreene.pbprdf.GamePeriodInfo

/**
 * Test the ShotPlay class
 *
 * @author andrewstellman
 */
class ShotPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "ShotPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  val testUri = TestUri.create("400610636")

  it should "parse shots" in {
    new ShotPlay(testUri, 4, 1, "9:18", "Mystics", "Stefanie Dolson misses 13-foot jumper", "0-0", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/4> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 9:18",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 42",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 558",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#shotBy -> http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://www.stellman-greene.com/pbprdf#shotType -> 13-foot jumper",
          "http://www.stellman-greene.com/pbprdf#shotMade -> false",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Stefanie Dolson misses 13-foot jumper"))

    new ShotPlay(testUri, 5, 1, "9:15", "Sun", "Kelsey Bone  misses", "0-0", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/5> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 9:15",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 45",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 555",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#shotBy -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://www.stellman-greene.com/pbprdf#shotMade -> false",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelsey Bone  misses"))

  }

  it should "parse assisted shots" in {
    new ShotPlay(testUri, 8, 2, "9:11", "Mystics", "Ivory Latta makes 24-foot three point jumper (Tierra Ruffin-Pratt assists)", "3-0", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/8> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 9:11",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 649",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 551",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#shotBy -> http://www.stellman-greene.com/pbprdf/players/Ivory_Latta",
          "http://www.stellman-greene.com/pbprdf#shotAssistedBy -> http://www.stellman-greene.com/pbprdf/players/Tierra_Ruffin-Pratt",
          "http://www.stellman-greene.com/pbprdf#shotType -> 24-foot three point jumper",
          "http://www.stellman-greene.com/pbprdf#shotMade -> true",
          "http://www.stellman-greene.com/pbprdf#shotPoints -> 3",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Ivory Latta makes 24-foot three point jumper (Tierra Ruffin-Pratt assists)"))
  }

  it should "parse the correct number of points for free throws" in {
    new ShotPlay(testUri, 88, 2, "9:15", "Sun", "Alyssa Thomas makes free throw 2 of 2", "18-26", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/88> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 9:15",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 645",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 555",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#shotBy -> http://www.stellman-greene.com/pbprdf/players/Alyssa_Thomas",
          "http://www.stellman-greene.com/pbprdf#shotType -> free throw 2 of 2",
          "http://www.stellman-greene.com/pbprdf#shotMade -> true",
          "http://www.stellman-greene.com/pbprdf#shotPoints -> 1",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Alyssa Thomas makes free throw 2 of 2"))
  }

}
