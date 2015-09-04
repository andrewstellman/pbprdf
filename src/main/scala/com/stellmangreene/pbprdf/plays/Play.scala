package com.stellmangreene.pbprdf.plays

import scala.util.matching.Regex
import com.stellmangreene.pbprdf.Event
import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.util.RdfOperations
import org.openrdf.model.URI
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.model.vocabulary.RDF
import com.stellmangreene.pbprdf.model.EntityUriFactory

/**
 * A Play is an event that matches a play-by-play regex, and can be checked to see if it
 * matches a description from a play-by-play
 *
 * @author andrewstellman
 */
abstract class Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Event(gameUri: URI, eventNumber, period, time, s"${team}: ${play}")
    with RdfOperations {

  /**
   * Add the type and pbprdf:team triples that every Play event must have
   */
  override def addRdf(rep: Repository) {
    rep.addTriple(eventUri, RDF.TYPE, Ontology.PLAY, EntityUriFactory.contextUri)
    rep.addTriple(eventUri, Ontology.TEAM, EntityUriFactory.getTeamUri(team), EntityUriFactory.contextUri)
    super.addRdf(rep)
  }
  
  def getTeam = team
  
}
