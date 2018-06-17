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
 * A play that represents a player entering the game
 * <p>
 * Examples:
 * Natasha Cloud enters the game for Ivory Latta
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
class EnterPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case EnterPlay.playByPlayRegex(playerEntering, playerExiting) => {
          Set(
            (eventIri, RDF.TYPE, Ontology.ENTERS),
            (eventIri, Ontology.PLAYER_ENTERING, EntityIriFactory.getPlayerIri(playerEntering)),
            (eventIri, Ontology.PLAYER_EXITING, EntityIriFactory.getPlayerIri(playerExiting)))
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