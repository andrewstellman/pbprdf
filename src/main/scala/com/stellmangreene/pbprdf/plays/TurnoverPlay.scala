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
class TurnoverPlay(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameIri: IRI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, IRI, Value)] =
      play match {
        case TurnoverPlay.playByPlayRegex(player, turnoverType, steals) => {

          val turnoverPlayerTriple: Set[(Resource, IRI, Value)] =
            if (!player.trim.isEmpty)
              if (player.trim == "shot clock")
                Set((eventIri, Ontology.TURNOVER_TYPE, rep.getValueFactory.createLiteral("shot clock")))
              else
                Set(
                  (eventIri, Ontology.TURNED_OVER_BY, EntityIriFactory.getPlayerIri(player)),
                  (eventIri, Ontology.TURNOVER_TYPE, rep.getValueFactory.createLiteral(turnoverType.trim.toLowerCase)))
            else
              Set()

          val stealsRegex = """ ?\((.*) steals\)""".r

          val stolenByTriple: Set[(Resource, IRI, Value)] =
            steals match {
              case stealsRegex(stolenByPlayer) =>
                Set((eventIri, Ontology.STOLEN_BY, EntityIriFactory.getPlayerIri(stolenByPlayer)))
              case _ => Set()
            }

          Set((eventIri, RDF.TYPE, Ontology.TURNOVER)) ++ turnoverPlayerTriple ++ stolenByTriple
        }

        case _ => { logger.warn(s"Unrecognized turnover play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object TurnoverPlay extends PlayMatcher {

  val playByPlayRegex = """(?i)^(.*?) +(turnover|lost ball turnover|traveling|turnover|bad pass|kicked ball violation|lane violation|turnover \(lane violation\)|double lane violation|jump ball violation|defensive goaltending violation|Out-of-Bounds Bad Pass)( ?.*)$""".r

}