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
 * A play that represents a jump ball
 * <p>
 * Examples:
 * Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)
 *
 * @param gameIri
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
 * @param gamePeriodInfo
 *        GamePeriodInfo for converting game time to seconds
 *
 * @author andrewstellman
 */
class JumpBallPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case JumpBallPlay.playByPlayRegex(awayPlayer, homePlayerAndGainsPossession) => {
          logger.debug(s"Parsing jump ball from play: ${play}")

          val gainsPossessionRegex = """(.*) \((.*) gains possession\)""".r

          homePlayerAndGainsPossession match {
            case gainsPossessionRegex(homePlayer, gainedPossessionPlayer) =>
              Set(
                (eventIri, RDF.TYPE, Ontology.JUMP_BALL),
                (eventIri, Ontology.JUMP_BALL_HOME_PLAYER, EntityIriFactory.getPlayerIri(homePlayer)),
                (eventIri, Ontology.JUMP_BALL_AWAY_PLAYER, EntityIriFactory.getPlayerIri(awayPlayer)),
                (eventIri, Ontology.JUMP_BALL_GAINED_POSSESSION, EntityIriFactory.getPlayerIri(gainedPossessionPlayer)))

            case _ => Set(
              (eventIri, RDF.TYPE, Ontology.JUMP_BALL),
              (eventIri, Ontology.JUMP_BALL_HOME_PLAYER, EntityIriFactory.getPlayerIri(homePlayerAndGainsPossession)),
              (eventIri, Ontology.JUMP_BALL_AWAY_PLAYER, EntityIriFactory.getPlayerIri(awayPlayer)))
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
