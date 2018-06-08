package com.stellmangreene.pbprdf.plays

import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.repository.Repository

import com.stellmangreene.pbprdf.model.EntityIriFactory
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
class TimeoutPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    logger.debug(s"Parsing timeout from play: ${play}")

    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case TimeoutPlay.playByPlayRegex("Official") => {
          Set(
            (eventIri, RDF.TYPE, Ontology.TIMEOUT),
            (eventIri, Ontology.IS_OFFICIAL, rep.getValueFactory.createLiteral(true)))
        }
        case TimeoutPlay.playByPlayRegex(description) if (description.endsWith("Full")) => {
          Set(
            (eventIri, RDF.TYPE, Ontology.TIMEOUT),
            (eventIri, Ontology.TIMEOUT_DURATION, rep.getValueFactory.createLiteral("Full")))
        }
        case TimeoutPlay.playByPlayRegex(description) if (description.endsWith("20 Sec.")) => {
          Set(
            (eventIri, RDF.TYPE, Ontology.TIMEOUT),
            (eventIri, Ontology.TIMEOUT_DURATION, rep.getValueFactory.createLiteral("20 Sec.")))
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