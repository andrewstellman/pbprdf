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
 * A play that represents a block
 * <p>
 * Examples:
 * Emma Meesseman blocks Camille Little 's 2-foot  jumper
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
class BlockPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations
    with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
    play match {
      case BlockPlay.playByPlayRegex(blockedBy, shotBy, shotType) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.SHOT),
          (eventUri, RDF.TYPE, Ontology.BLOCK),
          (eventUri, Ontology.SHOT_BY, rep.getValueFactory.createLiteral(shotBy.trim)),
          (eventUri, Ontology.SHOT_BLOCKED_BY, rep.getValueFactory.createLiteral(blockedBy.trim)))
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
object BlockPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) blocks (.*)'s (.*)$""".r

}