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
class TurnoverPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case TurnoverPlay.playByPlayRegex(player, turnoverType, steals) => {

          val turnoverPlayerTriple: Set[(Resource, URI, Value)] =
            if (!player.trim.isEmpty)
              if (player.trim == "shot clock")
                Set((eventUri, Ontology.TURNOVER_TYPE, rep.getValueFactory.createLiteral("shot clock")))
              else
                Set((eventUri, Ontology.TURNED_OVER_BY, EntityUriFactory.getPlayerUri(player)),
                  (eventUri, Ontology.TURNOVER_TYPE, rep.getValueFactory.createLiteral(turnoverType.trim)))
            else
              Set()

          val stealsRegex = """ ?\((.*) steals\)""".r

          val stolenByTriple: Set[(Resource, URI, Value)] =
            steals match {
              case stealsRegex(stolenByPlayer) =>
                Set((eventUri, Ontology.STOLEN_BY, EntityUriFactory.getPlayerUri(stolenByPlayer)))
              case _ => Set()
            }

          Set((eventUri, RDF.TYPE, Ontology.TURNOVER)) ++ turnoverPlayerTriple ++ stolenByTriple
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
object TurnoverPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*?) +(turnover|lost ball turnover|traveling|turnover|bad pass|kicked ball violation|lane violation|turnover \(lane violation\)|double lane violation|jump ball violation)( ?.*)$""".r

}