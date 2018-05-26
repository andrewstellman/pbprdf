package com.stellmangreene.pbprdf.model.test

import scala.language.postfixOps
import org.scalatest._
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.model.URI
import org.openrdf.model.impl.ValueFactoryImpl
import com.stellmangreene.pbprdf.model.OntologyClass
import com.stellmangreene.pbprdf.model.OntologyAnnotationHelper
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.model.OntologyRdfRepository

/**
 * @author andrewstellman
 */
class OntologyRdfRepositorySpec extends FlatSpec with Matchers with RdfOperations {

  behavior of "OntologyRdfRepository"

  it should "read classes" in {

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://www.stellman-greene.com/pbprdf#Game> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#Class",
          "http://www.w3.org/2000/01/rdf-schema#label -> A game"))

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://www.stellman-greene.com/pbprdf#Turnover> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#Class",
          "http://www.w3.org/2000/01/rdf-schema#subClassOf -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/2000/01/rdf-schema#subClassOf -> http://www.stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/2000/01/rdf-schema#label -> A turnover"))
  }

  it should "read properties" in {

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://www.stellman-greene.com/pbprdf#hasPlayer> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#ObjectProperty",
          "http://www.w3.org/2000/01/rdf-schema#domain -> http://www.stellman-greene.com/pbprdf#Roster",
          "http://www.w3.org/2000/01/rdf-schema#range -> http://www.stellman-greene.com/pbprdf#Player",
          "http://www.w3.org/2000/01/rdf-schema#label -> A player on a roster"))

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://www.stellman-greene.com/pbprdf#period> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#DatatypeProperty",
          "http://www.w3.org/2000/01/rdf-schema#domain -> http://www.stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/2000/01/rdf-schema#range -> http://www.w3.org/2001/XMLSchema#int",
          "http://www.w3.org/2000/01/rdf-schema#comment -> Regulation periods are 1 through 4, overtime periods start at 5",
          "http://www.w3.org/2000/01/rdf-schema#label -> The period the event occurred in"))

  }
}
