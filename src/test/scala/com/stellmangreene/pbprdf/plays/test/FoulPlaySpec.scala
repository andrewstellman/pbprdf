package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.plays.FoulPlay
import com.stellmangreene.pbprdf.test.TestUri

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the FoulPlay class
 *
 * @author andrewstellman
 */
class FoulPlaySpec extends FlatSpec with Matchers {

  behavior of "FoulPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  val testUri = TestUri.create("400610636")

  it should "parse fouls" in {
    new FoulPlay(testUri, 37, 1, "4:56", "Sun", "Camille Little personal foul  (Stefanie Dolson draws the foul)", "10-9", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/37> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 4:56",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 304",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 296",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Camille_Little",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Camille Little personal foul  (Stefanie Dolson draws the foul)"))
  }

  it should "parse offensive fouls" in {
    new FoulPlay(testUri, 46, 1, "3:51", "Sun", "Kelsey Bone offensive foul  (Stefanie Dolson draws the foul)", "12-11", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/46> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 3:51",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 369",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 231",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sun",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Kelsey_Bone",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> http://www.stellman-greene.com/pbprdf/players/Stefanie_Dolson",
          "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelsey Bone offensive foul  (Stefanie Dolson draws the foul)"))
  }

  it should "parse shooting fouls" in {
    new FoulPlay(testUri, 85, 2, "9:15", "Mystics", "Kayla Thornton shooting foul  (Alyssa Thomas draws the foul)", "18-26", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/85> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 9:15",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 645",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 555",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> http://www.stellman-greene.com/pbprdf/players/Alyssa_Thomas",
          "http://www.stellman-greene.com/pbprdf#isShootingFoul -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton shooting foul  (Alyssa Thomas draws the foul)"))
  }

  it should "parse offensive charges" in {
    new FoulPlay(testUri, 166, 2, "1:05", "Mystics", "Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)", "40-38", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/166> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1135",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 65",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Mystics",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Kayla_Thornton",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> http://www.stellman-greene.com/pbprdf/players/Jasmine_Thomas",
          "http://www.stellman-greene.com/pbprdf#isCharge -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)"))
  }

  it should "parse loose ball fouls" in {
    val testUri2 = TestUri.create("400610739")
    new FoulPlay(testUri2, 275, 3, "1:05", "Sparks", "Jantel Lavender loose ball foul (Sylvia Fowles draws the foul)", "54-59", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610739/275> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri2.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 3",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1735",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 65",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sparks",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Jantel_Lavender",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> http://www.stellman-greene.com/pbprdf/players/Sylvia_Fowles",
          "http://www.stellman-greene.com/pbprdf#isLooseBallFoul -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Jantel Lavender loose ball foul (Sylvia Fowles draws the foul)"))
  }

  it should "parse fouls when no player draws the foul" in {
    val testUri3 = TestUri.create("400539523")
    new FoulPlay(testUri3, 13, 1, "8:12", "Sparks", "Jenna O'Hea offensive foul", "8-8", GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400539523/13> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri3.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 8:12",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 108",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 492",
          "http://www.stellman-greene.com/pbprdf#forTeam -> http://www.stellman-greene.com/pbprdf/teams/Sparks",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> http://www.stellman-greene.com/pbprdf/players/Jenna_O'Hea",
          "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Jenna O'Hea offensive foul"))

  }

}
