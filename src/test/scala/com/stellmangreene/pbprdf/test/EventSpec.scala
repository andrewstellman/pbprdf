package com.stellmangreene.pbprdf.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.Event
import com.stellmangreene.pbprdf.GamePeriodInfo

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * Test the Event class
 *
 * @author andrewstellman
 */
class EventSpec extends FlatSpec with Matchers {

  behavior of "an instance of Event"

  it should "generate the event URI" in {
    val event = Event(TestUri.create("400610636"), 38, 1, "4:56", "Official timeout")(GamePeriodInfo.WNBAPeriodInfo)
    event.eventUri.stringValue should be("http://www.stellman-greene.com/pbprdf/400610636/38")
  }

  it should "populate the event fields" in {
    val event = Event(TestUri.create("400610636"), 38, 1, "4:56", "Official timeout")(GamePeriodInfo.WNBAPeriodInfo)
    event.eventNumber should be(38)
    event.period should be(1)
    event.time should be("4:56")
  }

  it should "generate RDF for an unmatched event" in {
    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    val testUri = TestUri.create("400610636")
    Event(testUri, 38, 1, "4:56", "Unmatched event")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(testUri, 119, 2, "7:05", "Unmatched event 2")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/38> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 1",
          "http://www.stellman-greene.com/pbprdf#time -> 4:56",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 304",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 296",
          "http://www.w3.org/2000/01/rdf-schema#label -> Unmatched event"))

    rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/119> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 7:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 775",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 425",
          "http://www.w3.org/2000/01/rdf-schema#label -> Unmatched event 2"))
  }

  it should "calculate the correct times for the start, middle, and end of a period" in {
    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    Event(TestUri.create("X"), 1, 1, "10:00", "1st quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 25, 1, "0:00", "end of 1st quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 50, 2, "10:00", "2nd quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 75, 2, "5:00", "halfway through 2nd quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 100, 3, "10:00", "3rd quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 150, 4, "10:00", "4th quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 175, 4, "0:00", "end of 4th quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 200, 5, "5:00", "first overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 225, 5, "0:00", "end of first overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 250, 6, "5:00", "second overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 275, 6, "2:30", "halfway through second overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 300, 7, "5:00", "third overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    Event(TestUri.create("X"), 350, 7, "0:00", "end of third overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

    rep
      .executeQuery("""
SELECT * { 
  ?s <http://www.stellman-greene.com/pbprdf#secondsIntoGame> ?secondsIntoGame .
  ?s <http://www.w3.org/2000/01/rdf-schema#label> ?label .
}""")
      .map(statement => (s"${statement.getValue("label").stringValue} -> ${statement.getValue("secondsIntoGame").stringValue}"))
      .toSet should be(
        Set(
          "1st quarter -> 0",
          "end of 1st quarter -> 600",
          "2nd quarter -> 600",
          "halfway through 2nd quarter -> 900",
          "3rd quarter -> 1200",
          "4th quarter -> 1800",
          "end of 4th quarter -> 2400",
          "first overtime -> 2400",
          "end of first overtime -> 2700",
          "second overtime -> 2700",
          "halfway through second overtime -> 2850",
          "third overtime -> 3000",
          "end of third overtime -> 3300"))
  }

  it should "add previous and next triples" in {
    val rep = new SailRepository(new MemoryStore)
    rep.initialize

    val testUri = TestUri.create("12345678")

    val events = Seq(
      Event(testUri, 1, 1, "9:59", "First event Q1")(GamePeriodInfo.WNBAPeriodInfo),
      Event(testUri, 2, 1, "7:30", "Second event Q2")(GamePeriodInfo.WNBAPeriodInfo),
      Event(testUri, 3, 1, "5:00", "Third event Q3")(GamePeriodInfo.WNBAPeriodInfo),
      Event(testUri, 4, 1, "2:30", "Fourth event Q4")(GamePeriodInfo.WNBAPeriodInfo),
      Event(testUri, 5, 1, "1.0", "Last event Q5")(GamePeriodInfo.WNBAPeriodInfo))

    Event.addPreviousAndNextTriples(rep, events)

    def stripSO(s: String) = s.replaceAll("^http://www.stellman-greene.com/pbprdf/12345678/", "").toInt
    def stripP(s: String) = s.replaceAll("^http://www.stellman-greene.com/pbprdf#", "")

    val statements = rep.statements.toSeq
      .map(s => (stripSO(s.getSubject.stringValue), stripP(s.getPredicate.stringValue), stripSO(s.getObject.stringValue)))
      .toSet

    statements.size should be(8)
    statements should contain(1, "nextEvent", 2)
    statements should contain(2, "previousEvent", 1)
    statements should contain(2, "nextEvent", 3)
    statements should contain(3, "previousEvent", 2)
    statements should contain(3, "nextEvent", 4)
    statements should contain(4, "previousEvent", 3)
    statements should contain(4, "nextEvent", 5)
    statements should contain(5, "previousEvent", 4)
  }

}