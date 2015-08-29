package com.stellmangreene.pbprdf.plays

import scala.util.matching.Regex
import com.stellmangreene.pbprdf.Event
import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.util.RdfOperations
import org.openrdf.model.URI
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.model.vocabulary.RDF

/**
 * A Play is an event that matches a play-by-play regex, and can be checked to see if it
 * matches a description from a play-by-play
 *
 * @author andrewstellman
 */
abstract class Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Event(gameId: String, eventNumber, period, time, s"${team}: ${play}")
    with RdfOperations {

  /**
   * Add the type and pbprdf:team triples that every Play event must have
   */
  override def addRdf(rep: Repository) {
    rep.addTriple(eventUri, RDF.TYPE, Ontology.PLAY)
    rep.addTriple(eventUri, Ontology.TEAM, rep.getValueFactory.createLiteral(team))
    super.addRdf(rep)
  }

}
