package com.stellmangreene.pbprdf.plays

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.repository.Repository

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.model.EntityIriFactory
import com.stellmangreene.pbprdf.model.Ontology

import com.stellmangreene.pbprdf.util.RdfOperations._

import com.typesafe.scalalogging.LazyLogging

/**
 * A play that represents a rebound
 * <p>
 * Examples:
 * Emma Meesseman defensive rebound
 * Tierra Ruffin-Pratt offensive rebound
 * Washington offensive team rebound
 * Washington defensive team rebound
 *
 * @param gameID
 *        Unique ID of the game
 * @param eventNumber
 *        Sequential number of this event
 * @param period
 *        Period this occurred in (overtime starts with period 5)
 * @param team
 *        Name of the team
 * @param play
 *        Description of the play (eg. "Alyssa Thomas makes free throw 2 of 2")
 * @param score
 *        Game score ("10-4") - CURRENTLY IGNORED
 *
 * @author andrewstellman
 */
class ReboundPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case ReboundPlay.playByPlayRegex(reboundedBy, reboundType, isTeam) => {
          logger.debug(s"Parsing rebound from play: ${play}")

          val offensiveReboundTriples: Set[(Resource, IRI, Value)] =
            if (reboundType.trim == "offensive")
              Set((eventIri, Ontology.IS_OFFENSIVE, rep.getValueFactory.createLiteral(true)))
            else
              Set()

          Set(
            (eventIri, RDF.TYPE, Ontology.REBOUND),
            (eventIri, Ontology.REBOUNDED_BY, EntityIriFactory.getPlayerIri(reboundedBy))) ++
            offensiveReboundTriples
        }
        case _ => { logger.warn(s"Unrecognized rebound play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object ReboundPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) (offensive|defensive) (team )?rebound$""".r

}