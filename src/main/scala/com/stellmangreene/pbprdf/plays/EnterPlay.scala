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
 * A play that represents a player entering the game
 * <p>
 * Examples:
 * Natasha Cloud enters the game for Ivory Latta
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
class EnterPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case EnterPlay.playByPlayRegex(playerEntering, playerExiting) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.ENTERS),
            (eventUri, Ontology.PLAYER_ENTERING, EntityUriFactory.getPlayerUri(playerEntering)),
            (eventUri, Ontology.PLAYER_EXITING, EntityUriFactory.getPlayerUri(playerExiting)))
        }

        case _ => {
          logger.warn(s"Unable to parse entering play: ${play}")
          Set()
        }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

  private val playersEnteringAndExiting =
    play match {
      case EnterPlay.playByPlayRegex(playerEntering, playerExiting) => {
        (Some(playerEntering.trim), Some(playerExiting.trim))
      }

      case _ => (None, None)
    }

  /** The player entering the game (None if there's a problem parsing the player) */
  val playerEntering: Option[String] = playersEnteringAndExiting._1

  /** The player exiting the game (None if there's a problem parsing the player) */
  val playerExiting: Option[String] = playersEnteringAndExiting._2
}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object EnterPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) enters the game for (.*)$""".r

}