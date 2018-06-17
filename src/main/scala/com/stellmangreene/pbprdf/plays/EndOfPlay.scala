package com.stellmangreene.pbprdf.plays

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.repository.Repository

import com.stellmangreene.pbprdf.GamePeriodInfo
import com.stellmangreene.pbprdf.model.Ontology

import com.stellmangreene.pbprdf.util.RdfOperations._

import com.typesafe.scalalogging.LazyLogging

/**
 * A play that represents the end of a period or game
 * <p>
 * Examples:
 * End of the 1st Quarter
 * End of Game
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
 *
 * @author andrewstellman
 */
class EndOfPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    logger.debug(s"Parsing timeout from play: ${play}")

    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case EndOfPlay.playByPlayRegex("Game") => {
          Set(
            (eventIri, RDF.TYPE, Ontology.END_OF_GAME))
        }
        case _ => {
          Set(
            (eventIri, RDF.TYPE, Ontology.END_OF_PERIOD))
        }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object EndOfPlay extends PlayMatcher {

  val playByPlayRegex = """^End of (.+)$""".r

}