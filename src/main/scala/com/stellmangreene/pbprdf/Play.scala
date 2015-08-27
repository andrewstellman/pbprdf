package com.stellmangreene.pbprdf

import org.openrdf.repository.sail.SailRepositoryConnection
import org.openrdf.repository.sail.SailRepository
import org.openrdf.model.vocabulary.RDF
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.model.Entities
import org.openrdf.model.Value
import org.openrdf.model.URI
import org.openrdf.model.Resource
import org.openrdf.model.ValueFactory
import com.typesafe.scalalogging.LazyLogging

/**
 * A play that can be parsed into RDF triples
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
class Play(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String)
    extends Event(gameId: String, eventNumber, period, time, s"${team}: ${play}")
    with RdfOperations with LazyLogging {

  override def addRdf(rep: Repository) = {
    val triples = generateTriples(rep.getValueFactory)
    rep.addTriples(triples, Entities.contextUri)
    super.addRdf(rep)
  }

  def generateTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val triples: Set[(Resource, URI, Value)] =
      Set((eventUri, RDF.TYPE, Ontology.PLAY),
        (eventUri, Ontology.TEAM, valueFactory.createLiteral(team))) ++
        parseJumpBallTriples(valueFactory) ++
        parseReboundTriples(valueFactory) ++
        parseShotTriples(valueFactory) ++
        parseBlockTriples(valueFactory) ++
        parseFoulTriples(valueFactory) ++
        parseTechnicalTriples(valueFactory) ++
        parseEnterTriples(valueFactory) ++
        parseTurnoverTriples(valueFactory)

    if (triples.size <= 1)
      logger.warn(s"No triples parsed from play: ${play}")

    triples
  }

  /**
   * Parse jump ball triples
   * <p>
   * Examples:
   * Stefanie Dolson vs. Kelsey Bone (Jasmine Thomas gains possession)
   */
  private def parseJumpBallTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val regex = """^(.*) vs. (.*) \((.*) gains possession\)$""".r

    play match {
      case regex(awayPlayer, homePlayer, gainsPossessionPlayer) => {
        logger.debug(s"Parsing jump ball from play: ${play}")
        val lostPossessionPlayer =
          if (awayPlayer.trim == gainsPossessionPlayer.trim)
            homePlayer
          else
            awayPlayer
        Set(
          (eventUri, RDF.TYPE, Ontology.JUMP_BALL),
          (eventUri, Ontology.JUMP_BALL_HOME_PLAYER, valueFactory.createLiteral(homePlayer)),
          (eventUri, Ontology.JUMP_BALL_AWAY_PLAYER, valueFactory.createLiteral(awayPlayer)),
          (eventUri, Ontology.JUMP_BALL_GAINED_POSSESSION, valueFactory.createLiteral(gainsPossessionPlayer)))
      }
      case _ => Set()
    }
  }

  /**
   * Parse rebound triples
   * <p>
   * Examples:
   * Emma Meesseman defensive rebound
   * Tierra Ruffin-Pratt offensive rebound
   * Washington offensive team rebound
   * Washington defensive team rebound
   */
  private def parseReboundTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val regex = """^(.*) (offensive|defensive) (team )?rebound$""".r

    play match {
      case regex(reboundedBy, reboundType, isTeam) => {
        logger.debug(s"Parsing rebound from play: ${play}")

        val offensiveReboundTriples: Set[(Resource, URI, Value)] =
          if (reboundType.trim == "offensive")
            Set((eventUri, Ontology.IS_OFFENSIVE, valueFactory.createLiteral(true)))
          else
            Set()

        Set(
          (eventUri, RDF.TYPE, Ontology.REBOUND),
          (eventUri, Ontology.REBOUNDED_BY, valueFactory.createLiteral(reboundedBy))) ++
          offensiveReboundTriples
      }
      case _ => Set()
    }
  }

  /**
   * Parse shot triples
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
   */
  private def parseShotTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val regex = """^(.*) (makes|misses) (.*?)( \(.* assists\))?$""".r

    play match {
      case regex(player, makesMisses, shotType, assists) => {
        logger.debug(s"Parsing shot from play: ${play}")

        val made =
          if (makesMisses.trim == "makes")
            true
          else
            false

        val pointsTriple: Set[(Resource, URI, Value)] =
          if (made && shotType.contains("free throw"))
            Set((eventUri, Ontology.SHOT_POINTS, valueFactory.createLiteral(1)))
          else if (made && shotType.contains("three point"))
            Set((eventUri, Ontology.SHOT_POINTS, valueFactory.createLiteral(3)))
          else if (made)
            Set((eventUri, Ontology.SHOT_POINTS, valueFactory.createLiteral(2)))
          else Set()

        val assistsRegex = """ *\( *(.*) assists\)""".r

        val assistedByTriple: Set[(Resource, URI, Value)] =
          assists match {
            case assistsRegex(assistedBy) =>
              Set((eventUri, Ontology.SHOT_ASSISTED_BY, valueFactory.createLiteral(assistedBy)))
            case _ => Set()
          }

        Set(
          (eventUri, RDF.TYPE, Ontology.SHOT),
          (eventUri, Ontology.SHOT_BY, valueFactory.createLiteral(player.trim)),
          (eventUri, Ontology.SHOT_MADE, valueFactory.createLiteral(made)),
          (eventUri, Ontology.SHOT_TYPE, valueFactory.createLiteral(shotType.trim))) ++
          pointsTriple ++
          assistedByTriple
      }
      case _ => Set()
    }
  }

  /**
   * Parse block triples
   * <p>
   * Examples:
   * Emma Meesseman blocks Camille Little 's 2-foot  jumper
   */
  private def parseBlockTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val regex = """^(.*) blocks (.*)'s (.*)$""".r

    play match {
      case regex(blockedBy, shotBy, shotType) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.SHOT),
          (eventUri, RDF.TYPE, Ontology.BLOCK),
          (eventUri, Ontology.SHOT_BY, valueFactory.createLiteral(shotBy.trim)),
          (eventUri, Ontology.SHOT_BLOCKED_BY, valueFactory.createLiteral(blockedBy.trim)))
      }
      case _ => Set()
    }
  }

  /**
   * Parse foul triples
   * <p>
   * Examples:
   * Camille Little personal foul  (Stefanie Dolson draws the foul)
   * Kelsey Bone offensive foul  (Stefanie Dolson draws the foul)
   * Kayla Thornton shooting foul  (Alyssa Thomas draws the foul)
   * Kayla Thornton offensive Charge  (Jasmine Thomas draws the foul)
   * Jantel Lavender loose ball foul (Sylvia Fowles draws the foul)
   */
  private def parseFoulTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val foulRegex = """^(.*) (personal foul|shooting foul|offensive foul|offensive Charge|loose ball foul) +\((.*) draws the foul\)$""".r

    play match {
      case foulRegex(committedBy, foulType, drawnBy) => {
        val isShootingFoulTriple: Set[(Resource, URI, Value)] =
          if (foulType.trim == "shooting foul")
            Set((eventUri, Ontology.IS_SHOOTING_FOUL, valueFactory.createLiteral(true)))
          else
            Set()

        val offensiveTriples: Set[(Resource, URI, Value)] =
          if (foulType.trim == "offensive foul")
            Set((eventUri, Ontology.IS_OFFENSIVE, valueFactory.createLiteral(true)))
          else if (foulType.trim == "offensive Charge") {
            Set((eventUri, Ontology.IS_OFFENSIVE, valueFactory.createLiteral(true)))
            Set((eventUri, Ontology.IS_CHARGE, valueFactory.createLiteral(true)))
          } else
            Set()

        val looseBallTriple: Set[(Resource, URI, Value)] =
          if (foulType.trim == "loose ball foul")
            Set((eventUri, Ontology.IS_LOOSE_BALL_FOUL, valueFactory.createLiteral(true)))
          else
            Set()

        Set(
          (eventUri, RDF.TYPE, Ontology.FOUL),
          (eventUri, Ontology.FOUL_COMMITTED_BY, valueFactory.createLiteral(committedBy)),
          (eventUri, Ontology.FOUL_DRAWN_BY, valueFactory.createLiteral(drawnBy))) ++
          isShootingFoulTriple ++ offensiveTriples ++ looseBallTriple
      }
      case _ => Set()
    }
  }

  /**
   * Parse turnover triples
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
   */
  private def parseTurnoverTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val turnoverRegex = """^(.*?) *(turnover|lost ball turnover|traveling|shot clock turnover|bad pass|kicked ball violation)( +\(.* steals\))?$""".r
    play match {
      case turnoverRegex(player, turnoverType, steals) => {

        val turnoverPlayerTriple: Set[(Resource, URI, Value)] =
          if (!player.trim.isEmpty)
            Set((eventUri, Ontology.TURNED_OVER_BY, valueFactory.createLiteral(player.trim)))
          else
            Set()

        val turnoverTypeTriple: Set[(Resource, URI, Value)] =
          turnoverType match {
            case "lost ball turnover"    => Set((eventUri, Ontology.IS_LOST_BALL, valueFactory.createLiteral(true)))
            case "bad pass"              => Set((eventUri, Ontology.IS_BAD_PASS, valueFactory.createLiteral(true)))
            case "traveling"             => Set((eventUri, Ontology.IS_TRAVEL, valueFactory.createLiteral(true)))
            case "kicked ball violation" => Set((eventUri, Ontology.IS_KICKED_BALL_VIOLATION, valueFactory.createLiteral(true)))
            case "shot clock turnover"   => Set((eventUri, Ontology.IS_SHOT_CLOCK_VIOLATION, valueFactory.createLiteral(true)))
            case _                       => Set()
          }

        val stealsRegex = """^\( (.*) steals\)""".r

        val stolenByTriple: Set[(Resource, URI, Value)] =
          steals match {
            case stealsRegex(stolenByPlayer) =>
              Set((eventUri, Ontology.STOLEN_BY, valueFactory.createLiteral(stolenByPlayer)))
            case _ => Set()
          }

        Set((eventUri, RDF.TYPE, Ontology.TURNOVER)) ++ turnoverPlayerTriple ++ turnoverTypeTriple ++ stolenByTriple
      }

      case _ => Set()
    }
  }

  /**
   * Parse enter triples
   * <p>
   * Examples:
   * Natasha Cloud enters the game for Ivory Latta
   */
  private def parseEnterTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val entersRegex = """^(.*) enters the game for (.*)$""".r

    play match {
      case entersRegex(playerEntering, playerExiting) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.ENTERS),
          (eventUri, Ontology.PLAYER_ENTERING, valueFactory.createLiteral(playerEntering)),
          (eventUri, Ontology.PLAYER_EXITING, valueFactory.createLiteral(playerExiting)))
      }

      case _ => Set()
    }
  }

  /**
   * Parse technical triples
   * <p>
   * Examples:
   * Kara Lawson defensive 3-seconds (Technical Foul)
   * Los Angeles delay of game violation
   * delay techfoul
   */
  private def parseTechnicalTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val technicalFoulRegex = """^(.*) +technical foul.*""".r
    val threeSecondRegex = """^(.*) (.*) 3-seconds +\(Technical Foul\)$""".r
    val delayRegex = """.*(delay techfoul|delay of game violation).*""".r

    play match {
      case technicalFoulRegex(committedBy) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
          (eventUri, Ontology.FOUL_COMMITTED_BY, valueFactory.createLiteral(committedBy)))
      }

      case threeSecondRegex(committedBy, offensiveDefensive) => {
        val offensiveTriples: Set[(Resource, URI, Value)] =
          if (offensiveDefensive.trim == "offensive")
            Set((eventUri, Ontology.IS_OFFENSIVE, valueFactory.createLiteral(true)))
          else
            Set()
        Set(
          (eventUri, Ontology.IS_THREE_SECOND, valueFactory.createLiteral(true)),
          (eventUri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
          (eventUri, Ontology.FOUL_COMMITTED_BY, valueFactory.createLiteral(committedBy))) ++ offensiveTriples
      }

      case delayRegex(matchingText) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.TECHNICAL_FOUL),
          (eventUri, Ontology.IS_DELAY_OF_GAME, valueFactory.createLiteral(true)))
      }

      case _ => Set()
    }
  }

}