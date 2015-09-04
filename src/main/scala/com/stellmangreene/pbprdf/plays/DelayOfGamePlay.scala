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
 * A play that represents a delay of game violation
 * <p>
 * Examples:
 * Los Angeles delay of game violation
 * delay techfoul
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
class DelayOfGamePlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations
    with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case DelayOfGamePlay.playByPlayRegex(matchingText) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
            (eventUri, Ontology.IS_DELAY_OF_GAME, rep.getValueFactory.createLiteral(true)))
        }

        case _ => Set()
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object DelayOfGamePlay extends PlayMatcher {

  val playByPlayRegex = """.*(delay techfoul|delay of game violation).*""".r

}