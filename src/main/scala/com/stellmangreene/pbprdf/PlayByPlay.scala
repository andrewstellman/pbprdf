package com.stellmangreene.pbprdf

import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.model.vocabulary.RDF
import com.stellmangreene.pbprdf.model.Entities
import org.openrdf.model.URI
import com.stellmangreene.pbprdf.util.RdfOperations

/**
 * Play by play
 *
 * @author andrewstellman
 */
abstract class PlayByPlay extends RdfOperations {
  /** Events from the play-by-play */
  val events: Seq[Event]

  /** URI of this game */
  val gameUri: URI

  /**
   * Add the events to an RDF repository
   *
   * @param rep
   *            Sesame repository to add the events to
   */
  def addRdf(rep: Repository) = {
    rep.addTriple(gameUri, RDF.TYPE, Ontology.GAME, Entities.contextUri)
  }

  /**
   * Add the roster triples to an RDF repository
   *
   * @param rep
   *            Sesame repository to add the events to
   */
  protected def addRosterTriples(rep: Repository) = {
    
  }

}