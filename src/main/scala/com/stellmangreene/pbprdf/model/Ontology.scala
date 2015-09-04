package com.stellmangreene.pbprdf.model

import org.openrdf.model.impl.ValueFactoryImpl

/**
 * RDF types
 *
 * @author andrewstellman
 */
object Ontology {

  //TODO: Add addRdf() method that generates the ontology in its own context
  
  private val valueFactory = ValueFactoryImpl.getInstance()

  val NAMESPACE = "http://www.stellman-greene.com/pbprdf#"

  val EVENT = valueFactory.createURI(NAMESPACE, "Event")
  val PLAY = valueFactory.createURI(NAMESPACE, "Play")
  val GAME = valueFactory.createURI(NAMESPACE, "Game")
  val IN_PLAY_BY_PLAY = valueFactory.createURI(NAMESPACE, "inPlayByPlay")
  val HAS_HOME_TEAM_ROSTER = valueFactory.createURI(NAMESPACE, "hasHomeTeamRoster")
  val HAS_AWAY_TEAM_ROSTER = valueFactory.createURI(NAMESPACE, "hasAwayTeamRoster")

  val ROSTER = valueFactory.createURI(NAMESPACE, "Roster")
  val ROSTER_TEAM = valueFactory.createURI(NAMESPACE, "rosterTeam")
  val HAS_PLAYER = valueFactory.createURI(NAMESPACE, "hasPlayer")
  val GAME_TIME = valueFactory.createURI(NAMESPACE, "gameTime")
  val GAME_LOCATION = valueFactory.createURI(NAMESPACE, "gameLocation")

  val PLAYER = valueFactory.createURI(NAMESPACE, "Player")

  val GAME_ROSTER_TEAM = valueFactory.createURI(NAMESPACE, "GameRosterTeam")
  val GAME_ROSTER_PLAYER = valueFactory.createURI(NAMESPACE, "GameRosterPlayer")
  val TEAM_IN_GAME = valueFactory.createURI(NAMESPACE, "teamInGame")
  val PLAYER_ON_TEAM = valueFactory.createURI(NAMESPACE, "playerOnTeam")

  val TEAM = valueFactory.createURI(NAMESPACE, "team")
  val TIME = valueFactory.createURI(NAMESPACE, "time")
  val PERIOD = valueFactory.createURI(NAMESPACE, "period")
  val SECONDS_INTO_GAME = valueFactory.createURI(NAMESPACE, "secondsIntoGame")

  val TIMEOUT = valueFactory.createURI(NAMESPACE, "Timeout")
  val TIMEOUT_TEAM = valueFactory.createURI(NAMESPACE, "timeoutTeam")
  val TIMEOUT_DURATION = valueFactory.createURI(NAMESPACE, "timeoutDuration")

  val JUMP_BALL = valueFactory.createURI(NAMESPACE, "JumpBall")
  val JUMP_BALL_HOME_PLAYER = valueFactory.createURI(NAMESPACE, "jumpBallHomePlayer")
  val JUMP_BALL_AWAY_PLAYER = valueFactory.createURI(NAMESPACE, "jumpBallAwayPlayer")
  val JUMP_BALL_GAINED_POSSESSION = valueFactory.createURI(NAMESPACE, "jumpBallGainedPossession")

  val IS_OFFENSIVE = valueFactory.createURI(NAMESPACE, "isOffensive") // for rebounds and 3-second fouls

  val REBOUND = valueFactory.createURI(NAMESPACE, "Rebound")
  val REBOUNDED_BY = valueFactory.createURI(NAMESPACE, "reboundedBy")

  val SHOT = valueFactory.createURI(NAMESPACE, "Shot")
  val BLOCK = valueFactory.createURI(NAMESPACE, "Block") // pbprdf:Block rdfs:isSubclassOf pbprdf:Shot
  val SHOT_MADE = valueFactory.createURI(NAMESPACE, "shotMade")
  val SHOT_BY = valueFactory.createURI(NAMESPACE, "shotBy")
  val SHOT_TYPE = valueFactory.createURI(NAMESPACE, "shotType")
  val SHOT_ASSISTED_BY = valueFactory.createURI(NAMESPACE, "shotAssistedBy")
  val SHOT_POINTS = valueFactory.createURI(NAMESPACE, "shotPoints")
  val SHOT_BLOCKED_BY = valueFactory.createURI(NAMESPACE, "shotBlockedBy")

  val FOUL = valueFactory.createURI(NAMESPACE, "Foul")
  val TECHNICAL_FOUL = valueFactory.createURI(NAMESPACE, "TechnicalFoul")
  val FOUL_COMMITTED_BY = valueFactory.createURI(NAMESPACE, "foulCommittedBy")
  val FOUL_DRAWN_BY = valueFactory.createURI(NAMESPACE, "foulDrawnBy")
  val IS_SHOOTING_FOUL = valueFactory.createURI(NAMESPACE, "isShootingFoul")
  val IS_LOOSE_BALL_FOUL = valueFactory.createURI(NAMESPACE, "isLooseBallFoul")
  val IS_CHARGE = valueFactory.createURI(NAMESPACE, "isCharge")
  val IS_THREE_SECOND = valueFactory.createURI(NAMESPACE, "isThreeSecond")
  val IS_DELAY_OF_GAME = valueFactory.createURI(NAMESPACE, "isDelayOfGame")
  val TECHNICAL_FOUL_NUMBER = valueFactory.createURI(NAMESPACE, "technicalFoulNumber")

  val ENTERS = valueFactory.createURI(NAMESPACE, "Enters")
  val PLAYER_ENTERING = valueFactory.createURI(NAMESPACE, "playerEntering")
  val PLAYER_EXITING = valueFactory.createURI(NAMESPACE, "playerExiting")

  val TURNOVER = valueFactory.createURI(NAMESPACE, "Turnover")
  val STOLEN_BY = valueFactory.createURI(NAMESPACE, "stolenBy")
  val TURNED_OVER_BY = valueFactory.createURI(NAMESPACE, "turnedOverBy")
  val TURNOVER_TYPE = valueFactory.createURI(NAMESPACE, "turnoverType")
}
