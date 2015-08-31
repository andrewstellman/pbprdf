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
 * A play that represents a shot
 * <p>
 * Examples:
 * Kelsey Bone  misses jumper
 * Stefanie Dolson  misses 13-foot jumper
 * Emma Meesseman makes 13-foot two point shot
 * Jasmine Thomas makes layup (Alex Bentley assists)
 * Kara Lawson makes 24-foot  three point jumper  (Ivory Latta assists)
 * Ivory Latta  misses finger roll layup
 * Natasha Cloud misses free throw 1 of 2
 * Alyssa Thomas makes free throw 2 of 2
 * Alex Bentley makes technical free throw
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
class ShotPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    with RdfOperations
    with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] =
      play match {
        case ShotPlay.playByPlayRegex(player, makesMisses, shotType, assists) => {
          logger.debug(s"Parsing shot from play: ${play}")

          val made =
            if (makesMisses.trim == "makes")
              true
            else
              false

          val pointsTriple: Set[(Resource, URI, Value)] =
            if (made && shotType.contains("free throw"))
              Set((eventUri, Ontology.SHOT_POINTS, rep.getValueFactory.createLiteral(1)))
            else if (made && shotType.contains("three point"))
              Set((eventUri, Ontology.SHOT_POINTS, rep.getValueFactory.createLiteral(3)))
            else if (made)
              Set((eventUri, Ontology.SHOT_POINTS, rep.getValueFactory.createLiteral(2)))
            else Set()

          val assistsRegex = """ *\( *(.*) assists\)""".r

          val assistedByTriple: Set[(Resource, URI, Value)] =
            assists match {
              case assistsRegex(assistedBy) =>
                Set((eventUri, Ontology.SHOT_ASSISTED_BY, rep.getValueFactory.createLiteral(assistedBy)))
              case _ => Set()
            }

          Set(
            (eventUri, RDF.TYPE, Ontology.SHOT),
            (eventUri, Ontology.SHOT_BY, rep.getValueFactory.createLiteral(player.trim)),
            (eventUri, Ontology.SHOT_MADE, rep.getValueFactory.createLiteral(made)),
            (eventUri, Ontology.SHOT_TYPE, rep.getValueFactory.createLiteral(shotType.trim))) ++
            pointsTriple ++
            assistedByTriple
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
object ShotPlay extends PlayMatcher {

  val playByPlayRegex = """^(.*) (makes|misses) (.*?)( \(.* assists\))?$""".r

}
