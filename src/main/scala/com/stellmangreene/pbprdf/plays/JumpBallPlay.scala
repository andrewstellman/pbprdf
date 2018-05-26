package com.stellmangreene.pbprdf.plays

import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.openrdf.model.vocabulary.RDF
import org.openrdf.repository.Repository

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology

import com.stellmangreene.pbprdf.util.RdfOperations._

import com.typesafe.scalalogging.LazyLogging

/**
 * A play that represents a jump ball
 * <p>
 * Examples:
 * Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)
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
class JumpBallPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case JumpBallPlay.playByPlayRegex(awayPlayer, homePlayerAndGainsPossession) => {
          logger.debug(s"Parsing jump ball from play: ${play}")

          val gainsPossessionRegex = """(.*) \((.*) gains possession\)""".r

          homePlayerAndGainsPossession match {
            case gainsPossessionRegex(homePlayer, gainedPossessionPlayer) =>
              Set(
                (eventUri, RDF.TYPE, Ontology.JUMP_BALL),
                (eventUri, Ontology.JUMP_BALL_HOME_PLAYER, EntityUriFactory.getPlayerUri(homePlayer)),
                (eventUri, Ontology.JUMP_BALL_AWAY_PLAYER, EntityUriFactory.getPlayerUri(awayPlayer)),
                (eventUri, Ontology.JUMP_BALL_GAINED_POSSESSION, EntityUriFactory.getPlayerUri(gainedPossessionPlayer)))

            case _ => Set(
              (eventUri, RDF.TYPE, Ontology.JUMP_BALL),
              (eventUri, Ontology.JUMP_BALL_HOME_PLAYER, EntityUriFactory.getPlayerUri(homePlayerAndGainsPossession)),
              (eventUri, Ontology.JUMP_BALL_AWAY_PLAYER, EntityUriFactory.getPlayerUri(awayPlayer)))
          }
        }

        case _ => { logger.warn(s"Unrecognized jump ball play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object JumpBallPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) vs. (.*)$""".r

}
