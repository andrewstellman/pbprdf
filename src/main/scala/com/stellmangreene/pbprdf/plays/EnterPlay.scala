package com.stellmangreene.pbprdf.plays

import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.openrdf.model.vocabulary.RDF
import org.openrdf.repository.Repository

import com.stellmangreene.pbprdf.model.Entities
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.util.RdfOperations
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
class EnterPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations
    with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case EnterPlay.playByPlayRegex(playerEntering, playerExiting) => {
          Set(
            (eventUri, RDF.TYPE, Ontology.ENTERS),
            (eventUri, Ontology.PLAYER_ENTERING, rep.getValueFactory.createLiteral(playerEntering)),
            (eventUri, Ontology.PLAYER_EXITING, rep.getValueFactory.createLiteral(playerExiting)))
        }

        case _ => Set()
      }

    if (!triples.isEmpty)
      rep.addTriples(triples, Entities.contextUri)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object EnterPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) enters the game for (.*)$""".r

}