package com.stellmangreene.pbprdf.model

import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import com.stellmangreene.pbprdf.util.RdfOperations
import org.openrdf.model.vocabulary.OWL
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.vocabulary.RDFS
import java.lang.reflect.Field
import org.openrdf.model.URI
import Ontology._

/**
 * Object that uses the Java annotations in Ontology to build an RDF repository
 * that contains statements that describe the ontology
 *
 * @author andrewstellman
 */
object OntologyRdfRepository extends RdfOperations {

  /** Repository that contains all of the ontology triples */
  val rep = new SailRepository(new MemoryStore)

  rep.initialize
  addRdfClasses
  addRdfProperties

  /** Add all of the RDF classes to the repository */
  private def addRdfClasses = {

    val ontologyClasses: Seq[(Field, OntologyClass)] = Ontology.getClass.getDeclaredFields
      .map(field => {
        field.setAccessible(true)
        (field, OntologyAnnotationHelper.getOntologyClassAnnotation(field))
      })
      .filter(_._2 != null)

    ontologyClasses.foreach(e => {
      val (field, ontologyClassAnnotation) = e
      val classUri: URI = field.get(Ontology).asInstanceOf[URI]

      rep.addTriple(classUri, RDF.TYPE, OWL.CLASS)
      rep.addTriple(classUri, RDFS.LABEL, rep.getValueFactory.createLiteral(ontologyClassAnnotation.label))

      val ontologySubClassOfAnnotation = OntologyAnnotationHelper.getOntologySubClassOfAnnotation(field)
      if (ontologySubClassOfAnnotation != null)
        ontologySubClassOfAnnotation.subClassOf.foreach(superClass => {
          rep.addTriple(classUri, RDFS.SUBCLASSOF, rep.getValueFactory.createURI(superClass))
        })

      val comment = OntologyAnnotationHelper.getComment(field)
      if (comment != null)
        rep.addTriple(classUri, RDFS.COMMENT, rep.getValueFactory.createLiteral(comment))
    })

  }

  /** Add all of the RDF properties to the repository */
  private def addRdfProperties = {

    val ontologyProperties: Seq[(Field, OntologyProperty)] = Ontology.getClass.getDeclaredFields
      .map(field => {
        field.setAccessible(true)
        (field, OntologyAnnotationHelper.getOntologyPropertyAnnotation(field))
      })
      .filter(_._2 != null)

    ontologyProperties.foreach(e => {
      val (field, ontologyPropertyAnnotation) = e
      val classUri: URI = field.get(Ontology).asInstanceOf[URI]

      rep.addTriple(classUri, RDFS.LABEL, rep.getValueFactory.createLiteral(ontologyPropertyAnnotation.label))
      rep.addTriple(classUri, RDFS.RANGE, rep.getValueFactory.createURI(ontologyPropertyAnnotation.range))

      if (ontologyPropertyAnnotation.domain != "")
        rep.addTriple(classUri, RDFS.DOMAIN, rep.getValueFactory.createURI(ontologyPropertyAnnotation.domain))

      if (!ontologyPropertyAnnotation.domains.isEmpty)
        ontologyPropertyAnnotation.domains.foreach(domain => {
          rep.addTriple(classUri, RDFS.DOMAIN, rep.getValueFactory.createURI(domain))
        })

      if (OntologyAnnotationHelper.isObjectProperty(field))
        rep.addTriple(classUri, RDF.TYPE, OWL.OBJECTPROPERTY)
      else
        rep.addTriple(classUri, RDF.TYPE, OWL.DATATYPEPROPERTY)

      val comment = OntologyAnnotationHelper.getComment(field)
      if (comment != null)
        rep.addTriple(classUri, RDFS.COMMENT, rep.getValueFactory.createLiteral(comment))
    })

  }

}