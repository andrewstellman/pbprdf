package com.stellmangreene.pbprdf.test

import java.io.FileInputStream
import org.scalatest._
import com.stellmangreene.pbprdf.EspnPlayByPlay
import com.stellmangreene.pbprdf.util.XmlHelper
import com.stellmangreene.pbprdf.plays.Play
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.repository.RepositoryResult
import info.aduna.iteration.Iteration
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.Event
import org.openrdf.model.Statement
import org.openrdf.query.BindingSet
import com.stellmangreene.pbprdf.GamePeriodInfo

/**
 * Test the Event class
 *
 * @author andrewstellman
 */
class EventSpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "an instance of Event"

  it should "generate the event URI" in {
    val event = new Event(TestUri.create("400610636"), 38, 1, "4:56", "Official timeout")(GamePeriodInfo.WNBAPeriodInfo)
    event.eventUri.stringValue should be("http://www.stellman-greene.com/pbprdf/400610636/38")
  }

  it should "populate the event fields" in {
    val event = new Event(TestUri.create("400610636"), 38, 1, "4:56", "Official timeout")(GamePeriodInfo.WNBAPeriodInfo)
    event.eventNumber should be(38)
    event.period should be(1)
    event.time should be("4:56")
  }

  it should "generate RDF for the event" in {
    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    val testUri = TestUri.create("400610636")
    new Event(testUri, 38, 1, "4:56", "Official timeout")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(testUri, 119, 2, "7:05", "Connecticut Full timeout")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

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
          "http://www.w3.org/2000/01/rdf-schema#label -> Official timeout"))

    rep
      .executeQuery("SELECT * { <http://www.stellman-greene.com/pbprdf/400610636/119> ?p ?o }")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.stellman-greene.com/pbprdf#Timeout",
          s"http://www.stellman-greene.com/pbprdf#inGame -> ${testUri.stringValue}",
          "http://www.stellman-greene.com/pbprdf#period -> 2",
          "http://www.stellman-greene.com/pbprdf#time -> 7:05",
          "http://www.stellman-greene.com/pbprdf#secondsIntoGame -> 775",
          "http://www.stellman-greene.com/pbprdf#secondsLeftInPeriod -> 425",
          "http://www.stellman-greene.com/pbprdf#timeoutTeam -> Connecticut",
          "http://www.stellman-greene.com/pbprdf#timeoutDuration -> Full",
          "http://www.w3.org/2000/01/rdf-schema#label -> Connecticut Full timeout"))
  }

  it should "calculate the correct times for the start, middle, and end of a period" in {
    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    new Event(TestUri.create("X"), 1, 1, "10:00", "1st quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 25, 1, "0:00", "end of 1st quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 50, 2, "10:00", "2nd quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 75, 2, "5:00", "halfway through 2nd quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 100, 3, "10:00", "3rd quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 150, 4, "10:00", "4th quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 175, 4, "0:00", "end of 4th quarter")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 200, 5, "5:00", "first overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 225, 5, "0:00", "end of first overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 250, 6, "5:00", "second overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 275, 6, "2:30", "halfway through second overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 300, 7, "5:00", "third overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)
    new Event(TestUri.create("X"), 350, 7, "0:00", "end of third overtime")(GamePeriodInfo.WNBAPeriodInfo).addRdf(rep)

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

}