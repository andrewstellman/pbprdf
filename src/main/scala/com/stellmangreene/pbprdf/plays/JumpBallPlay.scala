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
 * A play that represents a jump ball
 * <p>
 * Examples:
 * Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)
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
class JumpBallPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations
    with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case JumpBallPlay.playByPlayRegex(awayPlayer, homePlayer, gainsPossessionPlayer) => {
          logger.debug(s"Parsing jump ball from play: ${play}")
          val lostPossessionPlayer =
            if (awayPlayer.trim == gainsPossessionPlayer.trim)
              homePlayer
            else
              awayPlayer
          Set(
            (eventUri, RDF.TYPE, Ontology.JUMP_BALL),
            (eventUri, Ontology.JUMP_BALL_HOME_PLAYER, rep.getValueFactory.createLiteral(homePlayer)),
            (eventUri, Ontology.JUMP_BALL_AWAY_PLAYER, rep.getValueFactory.createLiteral(awayPlayer)),
            (eventUri, Ontology.JUMP_BALL_GAINED_POSSESSION, rep.getValueFactory.createLiteral(gainsPossessionPlayer)))
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
object JumpBallPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) vs. (.*) \((.*) gains possession\)$""".r

}
