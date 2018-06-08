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
 * A play that represents a technical violation
 * <p>
 * Examples:
 * Diana Taurasi technical foul(1st technical foul)
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
class TechnicalFoulPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case TechnicalFoulPlay.playByPlayRegex(committedBy, foulNumber) => {
          val committedByTriple: Set[(Resource, IRI, Value)] =
            if (committedBy != null && !committedBy.isEmpty)
              Set((eventIri, Ontology.FOUL_COMMITTED_BY, EntityIriFactory.getPlayerIri(committedBy)))
            else
              Set()

          Set(
            (eventIri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
            (eventIri, Ontology.TECHNICAL_FOUL_NUMBER, rep.getValueFactory.createLiteral(foulNumber.toInt))) ++ committedByTriple
        }

        case _ => { logger.warn(s"Unrecognized technical foul play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object TechnicalFoulPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) ?technical foul.*\((\d+)\D* technical foul\)""".r

}