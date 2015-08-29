package com.stellmangreene.pbprdf.plays.test

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.plays.BlockPlay
import com.stellmangreene.pbprdf.util.RdfOperations

/**
 * Test the BlockPlay class
 *
 * @author andrewstellman
 */
class BlockPlaySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "BlockPlay"

  // As long as each event has unique game and event IDs, they can all go into the same repository
  val rep = new SailRepository(new MemoryStore)
  rep.initialize

  it should "parse block triples" in {
    new BlockPlay("400610636", 108, 2, "7:48", "Mystics", "Tayler Hill blocks Jasmine Thomas's layup", "23-26").addRdf(rep)

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

}
