package com.stellmangreene.pbprdf.test

import java.io.FileInputStream
import org.scalatest._
import com.stellmangreene.pbprdf.EspnPlayByPlay
import com.stellmangreene.pbprdf.util.XmlHelper
import com.stellmangreene.pbprdf.Play
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.RepositoryResult
import info.aduna.iteration.Iteration
import com.stellmangreene.pbprdf.util.RdfOperations

/**
 * Test the Play class
 *
 * @author andrewstellman
 */
class PlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "an instance of Play"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse jump ball triples" in {
    val play = new Play("400610736", 426, 5, "4:58", "Mercury", "Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)", "78-78")
    play.addRdf(rep)
    
    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610736/426> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#JumpBall",
        "http://www.stellman-greene.com/pbprdf#period -> 5",
        "http://www.stellman-greene.com/pbprdf#time -> 4:58",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 2402",
        "http://www.stellman-greene.com/pbprdf#team -> Mercury",
        "http://www.stellman-greene.com/pbprdf#jumpBallHomePlayer -> Brittney Griner",
        "http://www.stellman-greene.com/pbprdf#jumpBallAwayPlayer -> Elena Delle Donne",
        "http://www.stellman-greene.com/pbprdf#jumpBallGainedPossession -> DeWanna Bonner",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mercury: Elena Delle Donne vs. Brittney Griner (DeWanna Bonner gains possession)"))
  }

  it should "parse rebound triples" in {
    val play = new Play("400610636", 125, 2, "4:02", "Mystics", "Emma Meesseman offensive rebound", "31-30")
    play.addRdf(rep)
    
    val statements = rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/125> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet

    statements should be(
      Set(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Rebound",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
        "http://www.stellman-greene.com/pbprdf#period -> 2",
        "http://www.stellman-greene.com/pbprdf#time -> 4:02",
        "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 958",
        "http://www.stellman-greene.com/pbprdf#team -> Mystics",
        "http://www.stellman-greene.com/pbprdf#reboundedBy -> Emma Meesseman",
        "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
        "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Emma Meesseman offensive rebound"))
  }

  it should "parse shot triples" in {
    new Play("400610636", 4, 1, "9:18", "Mystics", "Stefanie Dolson misses 13-foot jumper", "0-0").addRdf(rep)
    new Play("400610636", 8, 2, "9:11", "Mystics", "Ivory Latta makes 24-foot three point jumper (Tierra Ruffin-Pratt assists)", "3-0").addRdf(rep)
    new Play("400610636", 88, 2, "9:15", "Sun", "Alyssa Thomas makes free throw 2 of 2", "18-26").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/4> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 9:18",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 42",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#shotBy -> Stefanie Dolson",
          "http://www.stellman-greene.com/pbprdf#shotType -> 13-foot jumper",
          "http://www.stellman-greene.com/pbprdf#shotMade -> false",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Stefanie Dolson misses 13-foot jumper"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/8> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 9:11",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 649",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#shotBy -> Ivory Latta",
          "http://www.stellman-greene.com/pbprdf#shotAssistedBy -> Tierra Ruffin-Pratt",
          "http://www.stellman-greene.com/pbprdf#shotType -> 24-foot three point jumper",
          "http://www.stellman-greene.com/pbprdf#shotMade -> true",
          "http://www.stellman-greene.com/pbprdf#shotPoints -> 3",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Ivory Latta makes 24-foot three point jumper (Tierra Ruffin-Pratt assists)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/88> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 9:15",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 645",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#shotBy -> Alyssa Thomas",
          "http://www.stellman-greene.com/pbprdf#shotType -> free throw 2 of 2",
          "http://www.stellman-greene.com/pbprdf#shotMade -> true",
          "http://www.stellman-greene.com/pbprdf#shotPoints -> 1",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Alyssa Thomas makes free throw 2 of 2"))
  }

  it should "parse block triples" in {
    new Play("400610636", 108, 2, "7:48", "Mystics", "Tayler Hill blocks Jasmine Thomas's layup", "23-26").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/108> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Shot",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Block",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 7:48",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 732",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#shotBy -> Jasmine Thomas",
          "http://www.stellman-greene.com/pbprdf#shotBlockedBy -> Tayler Hill",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Tayler Hill blocks Jasmine Thomas's layup"))
  }

  it should "parse foul triples" in {
    new Play("400610636", 37, 1, "4:56", "Sun", "Camille Little personal foul  (Stefanie Dolson draws the foul)", "10-9").addRdf(rep)
    new Play("400610636", 46, 1, "3:51", "Sun", "Kelsey Bone offensive foul  (Stefanie Dolson draws the foul)", "12-11").addRdf(rep)
    new Play("400610636", 85, 2, "9:15", "Mystics", "Kayla Thornton shooting foul  (Alyssa Thomas draws the foul)", "18-26").addRdf(rep)
    new Play("400610636", 166, 2, "1:05", "Mystics", "Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)", "40-38").addRdf(rep)
    new Play("400610739", 275, 3, "1:05", "Sparks", "Jantel Lavender loose ball foul (Sylvia Fowles draws the foul)", "54-59").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/37> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 4:56",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 304",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Camille Little",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> Stefanie Dolson",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Camille Little personal foul  (Stefanie Dolson draws the foul)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/46> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 3:51",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 369",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Kelsey Bone",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> Stefanie Dolson",
          "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelsey Bone offensive foul  (Stefanie Dolson draws the foul)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/85> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 9:15",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 645",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Kayla Thornton",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> Alyssa Thomas",
          "http://www.stellman-greene.com/pbprdf#isShootingFoul -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton shooting foul  (Alyssa Thomas draws the foul)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/166> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1135",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Kayla Thornton",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> Jasmine Thomas",
          "http://www.stellman-greene.com/pbprdf#isCharge -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610739/275> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Foul",
          "http://www.stellman-greene.com/pbprdf#period -> 3",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1735",
          "http://www.stellman-greene.com/pbprdf#team -> Sparks",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Jantel Lavender",
          "http://www.stellman-greene.com/pbprdf#foulDrawnBy -> Sylvia Fowles",
          "http://www.stellman-greene.com/pbprdf#isLooseBallFoul -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Jantel Lavender loose ball foul (Sylvia Fowles draws the foul)"))
  }

  it should "parse turnover triples" in {
    new Play("400610636", 17, 1, "8:00", "Sun", "Tierra Ruffin-Pratt lost ball turnover (Alex Bentley steals)", "5-0").addRdf(rep)
    new Play("400610636", 84, 1, "9:36", "Sun", "shot clock turnover", "18-24").addRdf(rep)
    new Play("400610636", 167, 1, "1:05", "Mystics", "Kayla Thornton turnover", "40-38").addRdf(rep)
    new Play("400610636", 195, 2, "6:54", "Sun", "Alex Bentley bad pass", "52-40").addRdf(rep)
    new Play("400610636", 204, 2, "1:09", "Sun", "Kelsey Bone traveling", "52-42").addRdf(rep)
    new Play("400610636", 337, 3, "4:16", "Sun", "Kara Lawson kicked ball violation", "63-61").addRdf(rep)
    new Play("400610636", 366, 4, "8:04", "Mystics", "Ivory Latta bad pass (Kelsey Bone steals)", "69-66").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/17> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 8:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 120",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> Tierra Ruffin-Pratt",
          "http://www.stellman-greene.com/pbprdf#isLostBall -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Tierra Ruffin-Pratt lost ball turnover (Alex Bentley steals)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/84> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 9:36",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 24",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#isShotClockViolation -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: shot clock turnover"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/167> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 1:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 535",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> Kayla Thornton",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Kayla Thornton turnover"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/195> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 6:54",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 786",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> Alex Bentley",
          "http://www.stellman-greene.com/pbprdf#isBadPass -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Alex Bentley bad pass"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/204> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1131",
          "http://www.stellman-greene.com/pbprdf#time -> 1:09",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> Kelsey Bone",
          "http://www.stellman-greene.com/pbprdf#isTravel -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelsey Bone traveling"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/337> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#period -> 3",
          "http://www.stellman-greene.com/pbprdf#time -> 4:16",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1544",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> Kara Lawson",
          "http://www.stellman-greene.com/pbprdf#isKickedBallViolation -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kara Lawson kicked ball violation"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/366> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Turnover",
          "http://www.stellman-greene.com/pbprdf#period -> 4",
          "http://www.stellman-greene.com/pbprdf#time -> 8:04",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1916",
          "http://www.stellman-greene.com/pbprdf#team -> Mystics",
          "http://www.stellman-greene.com/pbprdf#turnedOverBy -> Ivory Latta",
          "http://www.stellman-greene.com/pbprdf#isBadPass -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Mystics: Ivory Latta bad pass (Kelsey Bone steals)"))

  }

  it should "parse enter triples" in {

    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    new Play("400610636", 101, 1, "8:00", "Sun", "Kelly Faris enters the game for Alyssa Thomas", "21-26").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/101> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Enters",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 8:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 120",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#playerEntering -> Kelly Faris",
          "http://www.stellman-greene.com/pbprdf#playerExiting -> Alyssa Thomas",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kelly Faris enters the game for Alyssa Thomas"))

  }

  it should "parse technical triples" in {
    new Play("400610636", 146, 1, "4:07", "Sun", "Kara Lawson offensive 3-seconds (Technical Foul)", "33-31").addRdf(rep)
    new Play("400610739", 86, 2, "10:00", "Sparks", "Los Angeles delay of game violation", "15-22").addRdf(rep)
    new Play("400610739", 295, 3, "1:39", "Sparks", "delay techfoul", "54-56").addRdf(rep)

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/146> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#TechnicalFoul",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 4:07",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 353",
          "http://www.stellman-greene.com/pbprdf#team -> Sun",
          "http://www.stellman-greene.com/pbprdf#foulCommittedBy -> Kara Lawson",
          "http://www.stellman-greene.com/pbprdf#isOffensive -> true",
          "http://www.stellman-greene.com/pbprdf#isThreeSecond -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sun: Kara Lawson offensive 3-seconds (Technical Foul)"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610739/86> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#TechnicalFoul",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 10:00",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 600",
          "http://www.stellman-greene.com/pbprdf#team -> Sparks",
          "http://www.stellman-greene.com/pbprdf#isDelayOfGame -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: Los Angeles delay of game violation"))

    rep.executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610739/295> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#TechnicalFoul",
          "http://www.stellman-greene.com/pbprdf#period -> 3",
          "http://www.stellman-greene.com/pbprdf#time -> 1:39",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 1701",
          "http://www.stellman-greene.com/pbprdf#team -> Sparks",
          "http://www.stellman-greene.com/pbprdf#isDelayOfGame -> true",
          "http://www.w3.org/2000/01/rdf-schema#label -> Sparks: delay techfoul"))

  }

}
