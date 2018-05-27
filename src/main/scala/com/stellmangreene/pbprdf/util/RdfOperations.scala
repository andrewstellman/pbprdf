package com.stellmangreene.pbprdf.util

import scala.language.implicitConversions
import org.openrdf.query.QueryLanguage
import org.openrdf.query.TupleQueryResult
import org.openrdf.repository.Repository
import info.aduna.iteration.Iteration
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.Resource
import org.openrdf.model.Value
import org.openrdf.model.URI
import org.openrdf.rio.RDFFormat
import java.io.FileOutputStream
import org.openrdf.rio.Rio
import org.openrdf.model.impl.LinkedHashModel
import info.aduna.iteration.Iterations
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.vocabulary.XMLSchema
import com.typesafe.scalalogging.LazyLogging

// TODO: Move this into a separate project, publish it to Maven Central, and add it as a dependency

/**
 * Define implicit operations to perform RDF functions on Sesame repositories and collections
 *
 * @author andrewstellman
 */
object RdfOperations {

  /**
   * Define implicit Repository operations
   */
  implicit def repositoryImplicitOperations(repository: Repository) = new RepositoryImplicitOperations(repository)

  /**
   * Define implicit Iteration operations
   */
  implicit def iterationImplicitOperations[T, X <: Exception](i: Iteration[T, X]) = new IterationImplicitOperations(i)

  /**
   * Implicit Repository operations to help execute SPARQL queries
   */
  class RepositoryImplicitOperations(repository: Repository) extends LazyLogging {

    /**
     * Returns all of the statements in the repository
     */
    def statements = {
      val conn = repository.getConnection
      conn.getStatements(null, null, null, true).asIterator
    }
    
    /**
     * Write all of the statements in the repository to a file or System.out
     *
     * @param outputFile
     *        The name of the file to write to, or None to write to System.out
     *
     * @param format
     *        The format to write (defaults to Turtle)
     */
    def writeAllStatements(outputFile: Option[String], rdfFormat: RDFFormat = RDFFormat.TURTLE) = {
      val conn = repository.getConnection
      var statements = conn.getStatements(null, null, null, true)

      var model = Iterations.addAll(statements, new LinkedHashModel)
      model.setNamespace("rdf", RDF.NAMESPACE)
      model.setNamespace("rdfs", RDFS.NAMESPACE)
      model.setNamespace("xsd", XMLSchema.NAMESPACE)
      model.setNamespace("pbprdf", Ontology.NAMESPACE)

      if (outputFile.isDefined) {
        val outputStream = new FileOutputStream(outputFile.get)
        logger.info(s"Writing Turtle to ${outputFile.get}")
        Rio.write(model, outputStream, rdfFormat)
      } else {
        logger.info("Writing Turtle to standard output")
        Rio.write(model, System.out, rdfFormat)
      }
    }

    /**
     * Execute a SPARQL query and return a result
     *
     * @param query
     *        Query to execute
     *
     * @return TupleQueryResult with the query results
     */
    def executeQuery(query: String): TupleQueryResult = {
      val conn = repository.getConnection
      val tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query)
      tupleQuery.evaluate
    }

    /**
     * Add a triple to the repository
     *
     * @param subject
     *        The statement's subject.
     * @param predicate
     *        The statement's predicate.
     * @param object
     *        The statement's object.
     * @throws RepositoryException
     *         If the data could not be added to the repository, for example
     *         because the repository is not writable.
     */
    def addTriple(subject: Resource, predicate: URI, `object`: Value): Unit = {
      val connection = repository.getConnection
      connection.add(subject, predicate, `object`)
    }

    /**
     * Add a triple to the repository
     *
     * @param subject
     *        The statement's subject.
     * @param predicate
     *        The statement's predicate.
     * @param object
     *        The statement's object.
     * @param context
     *        The context to add the data to
     * @throws RepositoryException
     *         If the data could not be added to the repository, for example
     *         because the repository is not writable.
     */
    def addTriple(subject: Resource, predicate: URI, `object`: Value, context: Resource): Unit = {
      val connection = repository.getConnection
      connection.add(subject, predicate, `object`, context)
    }

    /**
     * Add a set of triples to the repository
     *
     * @param triples
     *        Triples to add.
     * @throws RepositoryException
     *         If the data could not be added to the repository, for example
     *         because the repository is not writable.
     */
    def addTriples(triples: Set[(Resource, URI, Value)]): Unit = {
      val connection = repository.getConnection
      triples.foreach(triple => connection.add(triple._1, triple._2, triple._3))
    }

    /**
     * Add a set of triples to the repository
     *
     * @param triples
     *        Triples to add.
     * @param context
     *        The context to add the data to
     * @throws RepositoryException
     *         If the data could not be added to the repository, for example
     *         because the repository is not writable.
     */
    def addTriples(triples: Set[(Resource, URI, Value)], context: Resource): Unit = {
      val connection = repository.getConnection
      triples.foreach(triple => connection.add(triple._1, triple._2, triple._3, context))
    }
  }

  /**
   * Implicit Iteration[T, X] functions to help process RDF Iteration objects (like ReposotiryResult[Statement] collections)
   */
  protected class IterationImplicitOperations[T, X <: Exception](iteration: Iteration[T, X]) {
    private class IterationWrapper[T, X <: Exception](iteration: Iteration[T, X]) extends Iterator[T] {
      def hasNext: Boolean = iteration.hasNext
      def next(): T = iteration.next
    }

    private val iterator = for (statement <- new IterationWrapper[T, X](iteration)) yield statement

    /**
     * Converts this Iteration to an Iterator. May be lazy and unevaluated, and can be traversed only once.
     *
     * @returns an Iterator that returns all elements of this Iteration.
     */
    def asIterator(): Iterator[T] = {
      iterator
    }

    /**
     * Applies a function to this Iteration
     *
     * @param f
     *        the function that is applied for its side-effect to every element. The result of function f is discarded.
     *
     */
    def foreach[U](f: T => U): Unit = {
      iterator.foreach(f)
    }

    /**
     * Creates a new iterator that maps all produced values of this Iteration to new values using a transformation function
     *
     * @param f
     *        the transformation function
     *
     * @return a new iterator which transforms every value produced by this iterator by applying the function f to it.
     */
    def map[U](f: T => U): Iterator[U] = {
      iterator.map(f)
    }

    /**
     * Converts this Iteration to a sequence. As with toIterable, it's lazy in this default implementation, as this TraversableOnce may be lazy and unevaluated.
     *
     * @returns a sequence containing all elements of this Iteration.
     */
    def toSeq(): Seq[T] = {
      iterator.toSeq
    }

  }

}