package com.stellmangreene.pbprdf.model

import org.eclipse.rdf4j.model.impl.SimpleValueFactory

/**
 * RDF types
 *
 * @author andrewstellman
 */
object Ontology {

  private val valueFactory = SimpleValueFactory.getInstance

  val NAMESPACE = "http://stellman-greene.com/pbprdf#"

  // Prefixes

  @OntologyPrefix(prefix = "players")
  val PLAYERS_PREFIX = "http://stellman-greene.com/pbprdf/players/"

  // Basic entity types

  @OntologyClass(label = "A game")
  val GAME = valueFactory.createIRI(NAMESPACE, "Game")

  @OntologyClass(label = "An event in a play-by-play")
  var EVENT = valueFactory.createIRI(NAMESPACE, "Event")

  @OntologyClass(label = "A play in a play-by-play")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event"))
  val PLAY = valueFactory.createIRI(NAMESPACE, "Play")

  @OntologyClass(label = "A team")
  val TEAM = valueFactory.createIRI(NAMESPACE, "Team")

  @OntologyClass(label = "A player")
  val PLAYER = valueFactory.createIRI(NAMESPACE, "Player")

  // Properties and types for each game

  @OntologyProperty(
    label = "The game time",
    domain = "http://stellman-greene.com/pbprdf#Game",
    range = "http://www.w3.org/2001/XMLSchema#dateTime")
  val GAME_TIME = valueFactory.createIRI(NAMESPACE, "gameTime")

  @OntologyProperty(
    label = "The game location",
    domain = "http://stellman-greene.com/pbprdf#Game",
    range = "http://www.w3.org/2001/XMLSchema#string")
  val GAME_LOCATION = valueFactory.createIRI(NAMESPACE, "gameLocation")

  @OntologyProperty(
    label = "The home team for a game",
    domain = "http://stellman-greene.com/pbprdf#Game",
    range = "http://stellman-greene.com/pbprdf#Team")
  @OntologyObjectProperty
  val HOME_TEAM = valueFactory.createIRI(NAMESPACE, "homeTeam")

  @OntologyProperty(
    label = "The away team for a game",
    domain = "http://stellman-greene.com/pbprdf#Game",
    range = "http://stellman-greene.com/pbprdf#Team")
  @OntologyObjectProperty
  val AWAY_TEAM = valueFactory.createIRI(NAMESPACE, "awayTeam")

  @OntologyProperty(
    label = "The home team roster for a game",
    domain = "http://stellman-greene.com/pbprdf#Game",
    range = "http://stellman-greene.com/pbprdf#Roster")
  @OntologyObjectProperty
  @OntologyComment(comment = "The home team roster is typically represented by a bnode in the Game entity")
  val HAS_HOME_TEAM_ROSTER = valueFactory.createIRI(NAMESPACE, "hasHomeTeamRoster")

  @OntologyProperty(
    label = "The away team roster for a game",
    domain = "http://stellman-greene.com/pbprdf#Game",
    range = "http://stellman-greene.com/pbprdf#Roster")
  @OntologyObjectProperty
  @OntologyComment(comment = "The away team roster is typically represented by a bnode in the Game entity")
  val HAS_AWAY_TEAM_ROSTER = valueFactory.createIRI(NAMESPACE, "hasAwayTeamRoster")

  @OntologyClass(label = "A game roster")
  val ROSTER = valueFactory.createIRI(NAMESPACE, "Roster")

  @OntologyProperty(
    label = "The team for a home or away roster",
    domain = "http://stellman-greene.com/pbprdf#Roster",
    range = "http://stellman-greene.com/pbprdf#Team")
  @OntologyObjectProperty
  val ROSTER_TEAM = valueFactory.createIRI(NAMESPACE, "rosterTeam")

  @OntologyProperty(
    label = "A player on a roster",
    domain = "http://stellman-greene.com/pbprdf#Roster",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val HAS_PLAYER = valueFactory.createIRI(NAMESPACE, "hasPlayer")

  // Types and properties for events in a play-by-play

  @OntologyProperty(
    label = "The game the event occurred in",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://stellman-greene.com/pbprdf#Game")
  val IN_GAME = valueFactory.createIRI(NAMESPACE, "inGame")

  @OntologyProperty(
    label = "The team that an event is for",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://stellman-greene.com/pbprdf#Team")
  @OntologyObjectProperty
  val FOR_TEAM = valueFactory.createIRI(NAMESPACE, "forTeam")

  @OntologyProperty(
    label = "The game time of an event (eg. 9:37)",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#string")
  val TIME = valueFactory.createIRI(NAMESPACE, "time")

  @OntologyProperty(
    label = "The period the event occurred in",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  @OntologyComment(comment = "Regulation periods are 1 through 4, overtime periods start at 5")
  val PERIOD = valueFactory.createIRI(NAMESPACE, "period")

  @OntologyProperty(
    label = "The number of seconds into a game that an event occurred",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val SECONDS_INTO_GAME = valueFactory.createIRI(NAMESPACE, "secondsIntoGame")

  @OntologyProperty(
    label = "The number of seconds left in the period when an event occurred",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val SECONDS_LEFT_IN_PERIOD = valueFactory.createIRI(NAMESPACE, "secondsLeftInPeriod")

  // Types and properties for end of period or game

  @OntologyClass(label = "End of period")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val END_OF_PERIOD = valueFactory.createIRI(NAMESPACE, "EndOfPeriod")

  @OntologyClass(label = "End of game")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val END_OF_GAME = valueFactory.createIRI(NAMESPACE, "EndOfGame")

  // Types and properties for timeouts

  @OntologyClass(label = "A timeout")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val TIMEOUT = valueFactory.createIRI(NAMESPACE, "Timeout")

  @OntologyProperty(
    label = "The duration of a timeout",
    domain = "http://stellman-greene.com/pbprdf#Timeout",
    range = "http://www.w3.org/2001/XMLSchema#string")
  @OntologyComment(comment = "A string description (eg. Full timeout, 20 Sec.")
  val TIMEOUT_DURATION = valueFactory.createIRI(NAMESPACE, "timeoutDuration")

  @OntologyProperty(
    label = "Determines if a timeout is an official timeout",
    domains = Array("http://stellman-greene.com/pbprdf#Timeout"),
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "True for an official timeout")
  val IS_OFFICIAL = valueFactory.createIRI(NAMESPACE, "isOfficial")

  // Types and properties for jump balls

  @OntologyClass(label = "A jump ball")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val JUMP_BALL = valueFactory.createIRI(NAMESPACE, "JumpBall")

  @OntologyProperty(
    label = "The home player contesting the jump ball",
    domain = "http://stellman-greene.com/pbprdf#JumpBall",
    range = "http://stellman-greene.com/pbprdf#Player")
  val JUMP_BALL_HOME_PLAYER = valueFactory.createIRI(NAMESPACE, "jumpBallHomePlayer")

  @OntologyProperty(
    label = "The away player contesting the jump ball",
    domain = "http://stellman-greene.com/pbprdf#JumpBall",
    range = "http://stellman-greene.com/pbprdf#Player")
  val JUMP_BALL_AWAY_PLAYER = valueFactory.createIRI(NAMESPACE, "jumpBallAwayPlayer")

  @OntologyProperty(
    label = "The player who gained possession of the jump ball",
    domain = "http://stellman-greene.com/pbprdf#JumpBall",
    range = "http://stellman-greene.com/pbprdf#Player")
  val JUMP_BALL_GAINED_POSSESSION = valueFactory.createIRI(NAMESPACE, "jumpBallGainedPossession")

  // Rebounds

  @OntologyClass(label = "A rebound")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val REBOUND = valueFactory.createIRI(NAMESPACE, "Rebound")

  @OntologyProperty(
    label = "The player who rebounded the ball",
    domain = "http://stellman-greene.com/pbprdf#Rebound",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val REBOUNDED_BY = valueFactory.createIRI(NAMESPACE, "reboundedBy")

  @OntologyProperty(
    label = "Determines if a foul or rebound is offensive",
    domains = Array("http://stellman-greene.com/pbprdf#Foul", "http://stellman-greene.com/pbprdf#Rebound"),
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was defensive")
  val IS_OFFENSIVE = valueFactory.createIRI(NAMESPACE, "isOffensive")

  // Shots and blocks

  @OntologyClass(label = "A shot")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val SHOT = valueFactory.createIRI(NAMESPACE, "Shot")

  @OntologyClass(label = "A block")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play", "http://stellman-greene.com/pbprdf#Shot"))
  val BLOCK = valueFactory.createIRI(NAMESPACE, "Block") // pbprdf:Block rdfs:isSubclassOf pbprdf:Shot

  @OntologyProperty(
    label = "True if a shot was made",
    domain = "http://stellman-greene.com/pbprdf#Shot",
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Can have the value xsd:true for made shots or xsd:false for missed shots, will always be present for a shot")
  val SHOT_MADE = valueFactory.createIRI(NAMESPACE, "shotMade")

  @OntologyProperty(
    label = "The player that attempted the shot",
    domain = "http://stellman-greene.com/pbprdf#Shot",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val SHOT_BY = valueFactory.createIRI(NAMESPACE, "shotBy")

  @OntologyProperty(
    label = "The type of shot",
    domain = "http://stellman-greene.com/pbprdf#Shot",
    range = "http://www.w3.org/2001/XMLSchema#string")
  @OntologyComment(comment = "Will be a description such as layup or 21-foot jumper")
  val SHOT_TYPE = valueFactory.createIRI(NAMESPACE, "shotType")

  @OntologyProperty(
    label = "The player that assisted the shot",
    domain = "http://stellman-greene.com/pbprdf#Shot",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val SHOT_ASSISTED_BY = valueFactory.createIRI(NAMESPACE, "shotAssistedBy")

  @OntologyProperty(
    label = "The number of points scored by the shot",
    domain = "http://stellman-greene.com/pbprdf#Shot",
    range = "http://www.w3.org/2001/XMLSchema#int")
  @OntologyComment(comment = "Will only be present for made shots, will contain 1 for free throws, 2 for field goals, etc.")
  val SHOT_POINTS = valueFactory.createIRI(NAMESPACE, "shotPoints")

  @OntologyProperty(
    label = "The player that blocked the shot",
    domain = "http://stellman-greene.com/pbprdf#Block",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val SHOT_BLOCKED_BY = valueFactory.createIRI(NAMESPACE, "shotBlockedBy")

  // Fouls

  @OntologyClass(label = "A foul")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val FOUL = valueFactory.createIRI(NAMESPACE, "Foul")

  @OntologyProperty(
    label = "The player who committed the foul",
    domain = "http://stellman-greene.com/pbprdf#Foul",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val FOUL_COMMITTED_BY = valueFactory.createIRI(NAMESPACE, "foulCommittedBy")

  @OntologyProperty(
    label = "The player who drew the foul",
    domain = "http://stellman-greene.com/pbprdf#Foul",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val FOUL_DRAWN_BY = valueFactory.createIRI(NAMESPACE, "foulDrawnBy")

  @OntologyProperty(
    label = "Determines if a foul is a shooting foul",
    domain = "http://stellman-greene.com/pbprdf#Foul",
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was not a shooting foul")
  val IS_SHOOTING_FOUL = valueFactory.createIRI(NAMESPACE, "isShootingFoul")

  @OntologyProperty(
    label = "Determines if a foul is a loose ball foul",
    domain = "http://stellman-greene.com/pbprdf#Foul",
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was not a loose ball foul")
  val IS_LOOSE_BALL_FOUL = valueFactory.createIRI(NAMESPACE, "isLooseBallFoul")

  @OntologyProperty(
    label = "Determines if a foul is a charge",
    domain = "http://stellman-greene.com/pbprdf#Foul",
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was not a charge")
  val IS_CHARGE = valueFactory.createIRI(NAMESPACE, "isCharge")

  @OntologyProperty(
    label = "Determines if a foul is a personal blocking foul",
    domains = Array("http://stellman-greene.com/pbprdf#Foul"),
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was not a personal blocking foul")
  val IS_PERSONAL_BLOCKING_FOUL = valueFactory.createIRI(NAMESPACE, "isPersonalBlockingFoul")

  // Technicals

  @OntologyClass(label = "A technical foul")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val TECHNICAL_FOUL = valueFactory.createIRI(NAMESPACE, "TechnicalFoul")

  @OntologyProperty(
    label = "Determines if a technical foul is a three second violation",
    domain = "http://stellman-greene.com/pbprdf#TechnicalFoul",
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was not a three second violation")
  val IS_THREE_SECOND = valueFactory.createIRI(NAMESPACE, "isThreeSecond")

  @OntologyProperty(
    label = "Determines if a technical foul is a delay of game violation",
    domain = "http://stellman-greene.com/pbprdf#TechnicalFoul",
    range = "http://www.w3.org/2001/XMLSchema#boolean")
  @OntologyComment(comment = "Only ever has the value xsd:true, no value exists if the play was not a delay of game violation")
  val IS_DELAY_OF_GAME = valueFactory.createIRI(NAMESPACE, "isDelayOfGame")

  @OntologyProperty(
    label = "The technical foul number for a player",
    domain = "http://stellman-greene.com/pbprdf#TechnicalFoul",
    range = "http://www.w3.org/2001/XMLSchema#int")
  @OntologyComment(comment = "Will be 1 for first technical foul, 2 for 2nd")
  val TECHNICAL_FOUL_NUMBER = valueFactory.createIRI(NAMESPACE, "technicalFoulNumber")

  @OntologyClass(label = "A five-second violation")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val FIVE_SECOND_VIOLATION = valueFactory.createIRI(NAMESPACE, "FiveSecondViolation")

  // Ejection

  @OntologyClass(label = "An ejection")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val EJECTION = valueFactory.createIRI(NAMESPACE, "Ejection")

  @OntologyProperty(
    label = "The player that was ejected",
    domain = "http://stellman-greene.com/pbprdf#Ejection",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val PLAYER_EJECTED = valueFactory.createIRI(NAMESPACE, "playerEjected")

  // Players entering

  @OntologyClass(label = "A player entering the game")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val ENTERS = valueFactory.createIRI(NAMESPACE, "Enters")

  @OntologyProperty(
    label = "The player entering the game",
    domain = "http://stellman-greene.com/pbprdf#Enters",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val PLAYER_ENTERING = valueFactory.createIRI(NAMESPACE, "playerEntering")

  @OntologyProperty(
    label = "The player exiting the game",
    domain = "http://stellman-greene.com/pbprdf#Enters",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val PLAYER_EXITING = valueFactory.createIRI(NAMESPACE, "playerExiting")

  // Turnovers

  @OntologyClass(label = "A turnover")
  @OntologySubClassOf(subClassOf = Array("http://stellman-greene.com/pbprdf#Event", "http://stellman-greene.com/pbprdf#Play"))
  val TURNOVER = valueFactory.createIRI(NAMESPACE, "Turnover")

  @OntologyProperty(
    label = "The type of turnover",
    domain = "http://stellman-greene.com/pbprdf#Turnover",
    range = "http://www.w3.org/2001/XMLSchema#string")
  @OntologyComment(comment = "Contains text that describs the turnover (eg. traveling, kicked ball violation, etc.")
  val TURNOVER_TYPE = valueFactory.createIRI(NAMESPACE, "turnoverType")

  @OntologyProperty(
    label = "The player who stole the ball",
    domain = "http://stellman-greene.com/pbprdf#Turnover",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val STOLEN_BY = valueFactory.createIRI(NAMESPACE, "stolenBy")

  @OntologyProperty(
    label = "The player who turned the ball over",
    domain = "http://stellman-greene.com/pbprdf#Turnover",
    range = "http://stellman-greene.com/pbprdf#Player")
  @OntologyObjectProperty
  val TURNED_OVER_BY = valueFactory.createIRI(NAMESPACE, "turnedOverBy")

  // Previous and next events

  @OntologyProperty(
    label = "The event number",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val EVENT_NUMBER = valueFactory.createIRI(NAMESPACE, "eventNumber")

  @OntologyProperty(
    label = "The previous event in the game",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://stellman-greene.com/pbprdf#Event")
  @OntologyObjectProperty
  val PREVIOUS_EVENT = valueFactory.createIRI(NAMESPACE, "previousEvent")

  @OntologyProperty(
    label = "The next event in the game",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://stellman-greene.com/pbprdf#Event")
  @OntologyObjectProperty
  val NEXT_EVENT = valueFactory.createIRI(NAMESPACE, "nextEvent")

  @OntologyProperty(
    label = "The number of seconds since the previous event",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val SECONDS_SINCE_PREVIOUS_EVENT = valueFactory.createIRI(NAMESPACE, "secondsSincePreviousEvent")

  @OntologyProperty(
    label = "The number of seconds until the next event",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val SECONDS_UNTIL_NEXT_EVENT = valueFactory.createIRI(NAMESPACE, "secondsUntilNextEvent")

  // Scores

  @OntologyProperty(
    label = "Score for the home team",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val HOME_SCORE = valueFactory.createIRI(NAMESPACE, "homeScore")

  @OntologyProperty(
    label = "Score for the away team",
    domain = "http://stellman-greene.com/pbprdf#Event",
    range = "http://www.w3.org/2001/XMLSchema#int")
  val AWAY_SCORE = valueFactory.createIRI(NAMESPACE, "awayScore")

}
