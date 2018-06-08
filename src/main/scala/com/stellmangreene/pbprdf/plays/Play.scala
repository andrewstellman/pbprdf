package com.stellmangreene.pbprdf.plays

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.repository.Repository

import com.stellmangreene.pbprdf.Event
import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.model.EntityIriFactory
import com.stellmangreene.pbprdf.model.Ontology

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * A Play is an event that matches a play-by-play regex, and can be checked to see if it
 * matches a description from a play-by-play
 *
 * @author andrewstellman
 */
abstract class Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Event(gameIri: IRI, eventNumber, period, time, s"${team}: ${play}")(gamePeriodInfo, team, score, play) {

  /**
   * Add the type and pbprdf:team triples that every Play event must have
   */
  override def addRdf(rep: Repository) {
    rep.addTriple(eventIri, RDF.TYPE, Ontology.PLAY)
    rep.addTriple(eventIri, Ontology.FOR_TEAM, EntityIriFactory.getTeamIri(team))
    super.addRdf(rep)
  }

  def getTeam = team

}
