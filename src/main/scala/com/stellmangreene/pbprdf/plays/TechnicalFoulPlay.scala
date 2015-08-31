package com.stellmangreene.pbprdf.plays

import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.openrdf.model.vocabulary.RDF
import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.util.RdfOperations
import com.typesafe.scalalogging.LazyLogging

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
class TechnicalFoulPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations
    with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case TechnicalFoulPlay.playByPlayRegex(committedBy, foulNumber) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
            (eventUri, Ontology.TECHNICAL_FOUL_NUMBER, rep.getValueFactory.createLiteral(foulNumber.toInt)),
            (eventUri, Ontology.FOUL_COMMITTED_BY, rep.getValueFactory.createLiteral(committedBy)))
        }

        case _ => Set()
      }

    if (!triples.isEmpty)
      rep.addTriples(triples, EntityUriFactory.contextUri)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object TechnicalFoulPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) +technical foul.*\((\d+)\D* technical foul\)""".r

}