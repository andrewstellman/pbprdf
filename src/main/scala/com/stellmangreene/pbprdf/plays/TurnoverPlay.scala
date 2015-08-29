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
 * A play that represents a turnover
 * <p>
 * Examples:
 * Kelsey Bone  lost ball turnover (Emma Meesseman steals)
 * Kelsey Bone  turnover
 * shot clock turnover
 * Shekinna Stricklen  traveling
 * Stefanie Dolson  lost ball turnover (Kelsey Bone steals)
 * Camille Little  bad pass
 * Ivory Latta  bad pass (Kelsey Bone steals)
 * Kara Lawson kicked ball violation
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
class TurnoverPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case TurnoverPlay.playByPlayRegex(player, turnoverType, steals) => {

          val turnoverPlayerTriple: Set[(Resource, URI, Value)] =
            if (!player.trim.isEmpty)
              Set((eventUri, Ontology.TURNED_OVER_BY, rep.getValueFactory.createLiteral(player.trim)))
            else
              Set()

          val turnoverTypeTriple: Set[(Resource, URI, Value)] =
            turnoverType match {
              case "lost ball turnover"    => Set((eventUri, Ontology.IS_LOST_BALL, rep.getValueFactory.createLiteral(true)))
              case "bad pass"              => Set((eventUri, Ontology.IS_BAD_PASS, rep.getValueFactory.createLiteral(true)))
              case "traveling"             => Set((eventUri, Ontology.IS_TRAVEL, rep.getValueFactory.createLiteral(true)))
              case "kicked ball violation" => Set((eventUri, Ontology.IS_KICKED_BALL_VIOLATION, rep.getValueFactory.createLiteral(true)))
              case "shot clock turnover"   => Set((eventUri, Ontology.IS_SHOT_CLOCK_VIOLATION, rep.getValueFactory.createLiteral(true)))
              case _                       => Set()
            }

          val stealsRegex = """^\( (.*) steals\)""".r

          val stolenByTriple: Set[(Resource, URI, Value)] =
            steals match {
              case stealsRegex(stolenByPlayer) =>
                Set((eventUri, Ontology.STOLEN_BY, rep.getValueFactory.createLiteral(stolenByPlayer)))
              case _ => Set()
            }

          Set((eventUri, RDF.TYPE, Ontology.TURNOVER)) ++ turnoverPlayerTriple ++ turnoverTypeTriple ++ stolenByTriple
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
object TurnoverPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*?) *(turnover|lost ball turnover|traveling|shot clock turnover|bad pass|kicked ball violation)( +\(.* steals\))?$""".r

}