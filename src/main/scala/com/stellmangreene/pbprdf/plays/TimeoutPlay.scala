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
 * A play that represents a timeout
 * <p>
 * Examples:
 * Connecticut Full timeout
 * Washington 20 Sec. timeout
 * Official timeout
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
class TimeoutPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    logger.debug(s"Parsing timeout from play: ${play}")

    val triples: Set[(Resource, URI, Value)] =
      play match {
        case TimeoutPlay.playByPlayRegex("Official") => {
          Set(
            (eventUri, RDF.TYPE, Ontology.TIMEOUT),
            (eventUri, Ontology.IS_OFFICIAL, rep.getValueFactory.createLiteral(true)))
        }
        case TimeoutPlay.playByPlayRegex(description) if (description.endsWith("Full")) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.TIMEOUT),
            (eventUri, Ontology.TIMEOUT_DURATION, rep.getValueFactory.createLiteral("Full")))
        }
        case TimeoutPlay.playByPlayRegex(description) if (description.endsWith("20 Sec.")) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.TIMEOUT),
            (eventUri, Ontology.TIMEOUT_DURATION, rep.getValueFactory.createLiteral("20 Sec.")))
        }
        case _ => { logger.warn(s"Unrecognized timeout play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object TimeoutPlay extends PlayMatcher {

  val playByPlayRegex = """^(.+) timeout$""".r

}