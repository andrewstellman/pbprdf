package com.stellmangreene.pbprdf.plays

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.URI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.util.RdfOperations.repositoryImplicitOperations
import com.typesafe.scalalogging.LazyLogging
import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology
import org.eclipse.rdf4j.model.vocabulary.RDF


/**
 * A play that represents a block
 * <p>
 * Examples:
 * Emma Meesseman blocks Camille Little 's 2-foot  jumper
 * Krystal Thomas blocks Erin Phillips' 3-foot  layup
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
class BlockPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case BlockPlay.playByPlayRegex(blockedBy, shotBy, shotType) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.SHOT),
            (eventUri, RDF.TYPE, Ontology.BLOCK),
            (eventUri, Ontology.SHOT_BY, EntityUriFactory.getPlayerUri(shotBy)),
            (eventUri, Ontology.SHOT_BLOCKED_BY, EntityUriFactory.getPlayerUri(blockedBy)))
        }
        case _ => { logger.warn(s"Unrecognized block play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object BlockPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) blocks (.*)'s? (.*)$""".r

}