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
 * A play that represents a foul
 * <p>
 * Examples:
 * Camille Little personal foul  (Stefanie Dolson draws the foul)
 * Kelsey Bone offensive foul  (Stefanie Dolson draws the foul)
 * Kayla Thornton shooting foul  (Alyssa Thomas draws the foul)
 * Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)
 * Jantel Lavender loose ball foul (Sylvia Fowles draws the foul)
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
class FoulPlay(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  extends Play(gameUri: URI, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo)
  with RdfOperations
  with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case FoulPlay.playByPlayRegex(committedBy, foulType, drawnByGroup) => {

          val drawnByRegex = """ +\((.*) draws the foul\)""".r

          val drawnByTriple: Set[(Resource, URI, Value)] =
            drawnByGroup match {
              case drawnByRegex(drawnBy) => Set((eventUri, Ontology.FOUL_DRAWN_BY, EntityUriFactory.getPlayerUri(drawnBy)))
              case _                     => Set()
            }

          val isShootingFoulTriple: Set[(Resource, URI, Value)] =
            if (foulType.trim == "shooting foul")
              Set((eventUri, Ontology.IS_SHOOTING_FOUL, rep.getValueFactory.createLiteral(true)))
            else
              Set()

          val offensiveTriples: Set[(Resource, URI, Value)] =
            if (foulType.trim == "offensive foul")
              Set((eventUri, Ontology.IS_OFFENSIVE, rep.getValueFactory.createLiteral(true)))
            else if (foulType.trim == "offensive Charge") {
              Set((eventUri, Ontology.IS_OFFENSIVE, rep.getValueFactory.createLiteral(true)))
              Set((eventUri, Ontology.IS_CHARGE, rep.getValueFactory.createLiteral(true)))
            } else
              Set()

          val looseBallTriple: Set[(Resource, URI, Value)] =
            if (foulType.trim == "loose ball foul")
              Set((eventUri, Ontology.IS_LOOSE_BALL_FOUL, rep.getValueFactory.createLiteral(true)))
            else
              Set()

          Set(
            (eventUri, RDF.TYPE, Ontology.FOUL),
            (eventUri, Ontology.FOUL_COMMITTED_BY, EntityUriFactory.getPlayerUri(committedBy))) ++
            drawnByTriple ++ isShootingFoulTriple ++ offensiveTriples ++ looseBallTriple
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
object FoulPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) (personal foul|shooting foul|offensive foul|offensive Charge|loose ball foul|personal take foul|shooting block foul|personal block|in.?bound foul|away from play foul|clear path foul|flagrant foul type .)( +\(.* draws the foul\))?$""".r

}