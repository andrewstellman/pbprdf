package com.stellmangreene.pbprdf.model

import java.lang.reflect.Field

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.sail.memory.MemoryStore

import com.stellmangreene.pbprdf.util.RdfOperations.repositoryImplicitOperations

/**
 * Object that uses the Java annotations in Ontology to build an RDF repository
 * that contains statements that describe the ontology
 *
 * @author andrewstellman
 */
object OntologyRdfRepository {

  /** Repository that contains all of the ontology triples */
  val rep = new SailRepository(new MemoryStore)

  rep.initialize

  addRdfPrefixes(rep)
  addRdfClasses
  addRdfProperties

  /** Add all of the RDF prefixes to the repository */
  def addRdfPrefixes(rep: Repository) = {

    val ontologyPrefixes: Seq[(Field, OntologyPrefix)] = Ontology.getClass.getDeclaredFields
      .map(field => {
        field.setAccessible(true)
        (field, OntologyAnnotationHelper.getOntologyPrefixAnnotation(field))
      })
      .filter(_._2 != null)

    ontologyPrefixes.foreach(field => {
      val prefix = field._2.prefix
      val name = field._1.get(Ontology).toString
      rep.getConnection.setNamespace(prefix, name)
    })
  }

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
      val classIri: IRI = field.get(Ontology).asInstanceOf[IRI]

      rep.addTriple(classIri, RDF.TYPE, OWL.CLASS)
      rep.addTriple(classIri, RDFS.LABEL, rep.getValueFactory.createLiteral(ontologyClassAnnotation.label))

      val ontologySubClassOfAnnotation = OntologyAnnotationHelper.getOntologySubClassOfAnnotation(field)
      if (ontologySubClassOfAnnotation != null)
        ontologySubClassOfAnnotation.subClassOf.foreach(superClass => {
          rep.addTriple(classIri, RDFS.SUBCLASSOF, rep.getValueFactory.createIRI(superClass))
        })

      val comment = OntologyAnnotationHelper.getComment(field)
      if (comment != null)
        rep.addTriple(classIri, RDFS.COMMENT, rep.getValueFactory.createLiteral(comment))
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
      val classIri: IRI = field.get(Ontology).asInstanceOf[IRI]

      rep.addTriple(classIri, RDFS.LABEL, rep.getValueFactory.createLiteral(ontologyPropertyAnnotation.label))
      rep.addTriple(classIri, RDFS.RANGE, rep.getValueFactory.createIRI(ontologyPropertyAnnotation.range))

      if (ontologyPropertyAnnotation.domain != "")
        rep.addTriple(classIri, RDFS.DOMAIN, rep.getValueFactory.createIRI(ontologyPropertyAnnotation.domain))

      if (!ontologyPropertyAnnotation.domains.isEmpty)
        ontologyPropertyAnnotation.domains.foreach(domain => {
          rep.addTriple(classIri, RDFS.DOMAIN, rep.getValueFactory.createIRI(domain))
        })

      if (OntologyAnnotationHelper.isObjectProperty(field))
        rep.addTriple(classIri, RDF.TYPE, OWL.OBJECTPROPERTY)
      else
        rep.addTriple(classIri, RDF.TYPE, OWL.DATATYPEPROPERTY)

      val comment = OntologyAnnotationHelper.getComment(field)
      if (comment != null)
        rep.addTriple(classIri, RDFS.COMMENT, rep.getValueFactory.createLiteral(comment))
    })

  }

}