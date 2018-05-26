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
import com.stellmangreene.pbprdf.GamePeriodInfo

/**
 * A play that represents a rebound
 * <p>
 * Examples:
 * Emma Meesseman defensive rebound
 * Tierra Ruffin-Pratt offensive rebound
 * Washington offensive team rebound
 * Washington defensive team rebound
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
class ReboundPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with RdfOperations
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case ReboundPlay.playByPlayRegex(reboundedBy, reboundType, isTeam) => {
          logger.debug(s"Parsing rebound from play: ${play}")

          val offensiveReboundTriples: Set[(Resource, URI, Value)] =
            if (reboundType.trim == "offensive")
              Set((eventUri, Ontology.IS_OFFENSIVE, rep.getValueFactory.createLiteral(true)))
            else
              Set()

          Set(
            (eventUri, RDF.TYPE, Ontology.REBOUND),
            (eventUri, Ontology.REBOUNDED_BY, EntityUriFactory.getPlayerUri(reboundedBy))) ++
            offensiveReboundTriples
        }
        case _ => { logger.warn(s"Unrecognized rebound play: ${play}"); Set() }
      }

    if (!triples.isEmpty)
      rep.addTriples(triples)

    super.addRdf(rep)
  }

}

/**
 * Companion object that defines a regex that can be used to check this play against a description
 */
object ReboundPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) (offensive|defensive) (team )?rebound$""".r

}