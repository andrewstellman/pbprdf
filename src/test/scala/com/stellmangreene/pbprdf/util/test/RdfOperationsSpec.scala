package com.stellmangreene.pbprdf.util.test

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import scala.language.postfixOps

import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.util.RdfOperations._
import org.eclipse.rdf4j.rio.RDFFormat

/**
 * Unit tests for the RdfOperations trait that provides implicit RDF operations
 * for rdf4j repositories and Aduna Iterators such as TupleQueryResult objects
 *
 * @author andrewstellman
 */
class RdfOperationsSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  behavior of "RdfOperations"

  private var rep: Repository = null

  override def beforeEach() = {
    rep = new SailRepository(new MemoryStore)
    rep.initialize

    // Use the implicit functions to add triples

    rep.addTriple(rep.getValueFactory.createIRI("test:entity1"), RDF.TYPE, OWL.THING)
    rep.addTriples(Set(
      (rep.getValueFactory.createIRI("test:entity1"), RDFS.LABEL, rep.getValueFactory.createLiteral("This is a label")),
      (rep.getValueFactory.createIRI("test:entity1"), RDFS.COMMENT, rep.getValueFactory.createLiteral("This is a comment"))))

    // Use the implicit functions to add triples with a context

    val context = rep.getValueFactory.createIRI("test:context")
    rep.addTriple(rep.getValueFactory.createIRI("test:entity2"), RDF.TYPE, OWL.THING, context)
    rep.addTriples(
      Set(
        (rep.getValueFactory.createIRI("test:entity2"), rep.getValueFactory.createIRI("test:predicate#is"), rep.getValueFactory.createLiteral(true)),
        (rep.getValueFactory.createIRI("test:entity2"), RDFS.LABEL, rep.getValueFactory.createLiteral("This is a second label")),
        (rep.getValueFactory.createIRI("test:entity2"), RDFS.COMMENT, rep.getValueFactory.createLiteral("This is a second comment"))),
      context)
  }

  it should "execute a SPARQL query and iterate over the results" in {
    val results = rep.executeQuery("""
SELECT ?label ?comment
FROM NAMED <test:context>
WHERE {
   GRAPH ?context {
      OPTIONAL { ?s rdfs:label ?label }
      OPTIONAL { ?s rdfs:comment ?comment }
   }
}
""")

    results.map(bindingSet => s"label=${bindingSet.getValue("label")} comment=${bindingSet.getValue("comment")}").toSeq should be(
      Seq("""label="This is a second label"^^<http://www.w3.org/2001/XMLSchema#string> comment="This is a second comment"^^<http://www.w3.org/2001/XMLSchema#string>"""))
  }

  it should "treat Aduna iterations (like TupleQueryResult objects) as an iterator that can only be traversed once" in {
    val results = rep.executeQuery("SELECT * { ?s ?p ?o }")
    results.asIterator.isTraversableAgain should be(false)

    // The iterator can be used once
    results.asIterator.toSeq.size should be(7)

    // This is a true iterator: the first iteration is lazy, and traversing it a second time will yield no results
    results.asIterator.toSeq.isEmpty should be(true)

    // The toSeq, foreach, and map functions are just conveniences that call asIterator
    results.toSeq.isEmpty should be(true)
    results.map(e => e).isEmpty should be(true)
  }

  it should "write all of the statements in the repository to an RDF format" in {
    // Redirect stdout to a stream
    var myOut = new ByteArrayOutputStream
    System.setOut(new PrintStream(myOut));

    // Write all statements to stdout
    rep.writeAllStatements(None, RDFFormat.NQUADS)

    // Verify that stdout contains all of the expected nquads
    var standardOutput = myOut.toString
    standardOutput.split("\n")
      .toSet
      .filter(!_.contains("Writing Turtle to standard output")) /* remove the log message */ should be(
        Set(
          """<test:entity1> <http://www.w3.org/2000/01/rdf-schema#label> "This is a label" .""",
          """<test:entity1> <http://www.w3.org/2000/01/rdf-schema#comment> "This is a comment" .""",
          """<test:entity1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> .""",
          """<test:entity2> <http://www.w3.org/2000/01/rdf-schema#label> "This is a second label" <test:context> .""",
          """<test:entity2> <http://www.w3.org/2000/01/rdf-schema#comment> "This is a second comment" <test:context> .""",
          """<test:entity2> <test:predicate#is> "true"^^<http://www.w3.org/2001/XMLSchema#boolean> <test:context> .""",
          """<test:entity2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing> <test:context> ."""))
  }

  it should "get an iterator with all of the statements in the repository" in {
    val statements = rep.statements.toSet
    statements.map(_.toString) should be(
      Set(
        """(test:entity1, http://www.w3.org/2000/01/rdf-schema#comment, "This is a comment"^^<http://www.w3.org/2001/XMLSchema#string>) [null]""",
        """(test:entity1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/2002/07/owl#Thing) [null]""",
        """(test:entity1, http://www.w3.org/2000/01/rdf-schema#label, "This is a label"^^<http://www.w3.org/2001/XMLSchema#string>) [null]""",
        """(test:entity2, test:predicate#is, "true"^^<http://www.w3.org/2001/XMLSchema#boolean>) [test:context]""",
        """(test:entity2, http://www.w3.org/2000/01/rdf-schema#comment, "This is a second comment"^^<http://www.w3.org/2001/XMLSchema#string>) [test:context]""",
        """(test:entity2, http://www.w3.org/2000/01/rdf-schema#label, "This is a second label"^^<http://www.w3.org/2001/XMLSchema#string>) [test:context]""",
        """(test:entity2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/2002/07/owl#Thing) [test:context]"""))
  }

}
