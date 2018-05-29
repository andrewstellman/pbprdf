package com.stellmangreene.pbprdf.model.test

import scala.language.postfixOps

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import com.stellmangreene.pbprdf.model.OntologyRdfRepository

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * @author andrewstellman
 */
class OntologyRdfRepositorySpec extends FlatSpec with Matchers {

  behavior of "OntologyRdfRepository"

  it should "read classes" in {

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://stellman-greene.com/pbprdf#Game> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#Class",
          "http://www.w3.org/2000/01/rdf-schema#label -> A game"))

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://stellman-greene.com/pbprdf#Turnover> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#Class",
          "http://www.w3.org/2000/01/rdf-schema#subClassOf -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/2000/01/rdf-schema#subClassOf -> http://stellman-greene.com/pbprdf#Play",
          "http://www.w3.org/2000/01/rdf-schema#label -> A turnover"))
  }

  it should "read properties" in {

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://stellman-greene.com/pbprdf#hasPlayer> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#ObjectProperty",
          "http://www.w3.org/2000/01/rdf-schema#domain -> http://stellman-greene.com/pbprdf#Roster",
          "http://www.w3.org/2000/01/rdf-schema#range -> http://stellman-greene.com/pbprdf#Player",
          "http://www.w3.org/2000/01/rdf-schema#label -> A player on a roster"))

    OntologyRdfRepository.rep.executeQuery("SELECT * {<http://stellman-greene.com/pbprdf#period> ?p ?o}")
      .map(statement => (s"${statement.getValue("p").stringValue} -> ${statement.getValue("o").stringValue}"))
      .toSet should be(
        Set(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type -> http://www.w3.org/2002/07/owl#DatatypeProperty",
          "http://www.w3.org/2000/01/rdf-schema#domain -> http://stellman-greene.com/pbprdf#Event",
          "http://www.w3.org/2000/01/rdf-schema#range -> http://www.w3.org/2001/XMLSchema#int",
          "http://www.w3.org/2000/01/rdf-schema#comment -> Regulation periods are 1 through 4, overtime periods start at 5",
          "http://www.w3.org/2000/01/rdf-schema#label -> The period the event occurred in"))

  }
}
