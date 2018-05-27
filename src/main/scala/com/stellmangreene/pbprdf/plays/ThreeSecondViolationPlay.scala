package com.stellmangreene.pbprdf.plays

import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.openrdf.model.vocabulary.RDF
import org.openrdf.repository.Repository

import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.typesafe.scalalogging.LazyLogging
import com.stellmangreene.pbprdf.GamePeriodInfo

import com.stellmangreene.pbprdf.util.RdfOperations._

/**
 * A play that represents a three second violation
 * <p>
 * Examples:
 * Kara Lawson defensive 3-seconds (Technical Foul)
 * Rudy Gobert defensive 3-seconds (technical foul)
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
class ThreeSecondViolationPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case ThreeSecondViolationPlay.playByPlayRegex(committedBy) => {
          Set(
            (eventUri, Ontology.IS_THREE_SECOND, rep.getValueFactory.createLiteral(true)),
            (eventUri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
            (eventUri, Ontology.FOUL_COMMITTED_BY, EntityUriFactory.getPlayerUri(committedBy)))
        }

        case _ => { logger.warn(s"Unrecognized three second violation play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object ThreeSecondViolationPlay extends PlayMatcher {

  val playByPlayRegex = """(?i)^(.*) defensive 3-seconds +\(Technical Foul\)$""".r

}