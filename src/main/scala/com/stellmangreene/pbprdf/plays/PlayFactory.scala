package com.stellmangreene.pbprdf.plays

import com.stellmangreene.pbprdf.Event
import com.stellmangreene.pbprdf.util.RdfOperations
import com.typesafe.scalalogging.LazyLogging
import org.openrdf.model.URI
import com.stellmangreene.pbprdf.GamePeriodInfo

/**
 * Factory to create Play objects, choosing the subclass based on the play description
 *
 * @author andrewstellman
 */
object PlayFactory extends LazyLogging with RdfOperations {

  /**
   * Create an instance of a play class, choosing the specific class based on the play description
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
   *        Game score ("10-4")
   * @param gamePeriodInfo
   *        Period length in minutes
   *
   * @author andrewstellman
   */
  def createPlay(gameUri: URI, filename: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo): Event = {

    val trimmedPlay = play.trim.replaceAll(" +", " ")
    
    if (BlockPlay.matches(play))
      new BlockPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (DelayOfGamePlay.matches(play))
      new DelayOfGamePlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (EnterPlay.matches(play))
      new EnterPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (FoulPlay.matches(play))
      new FoulPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (JumpBallPlay.matches(play))
      new JumpBallPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (ReboundPlay.matches(play))
      new ReboundPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (ShotPlay.matches(play))
      new ShotPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (DoubleTechnicalFoulPlay.matches(play))
      new DoubleTechnicalFoulPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (TechnicalFoulPlay.matches(play))
      new TechnicalFoulPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (ThreeSecondViolationPlay.matches(play))
      new ThreeSecondViolationPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else if (TurnoverPlay.matches(play))
      new TurnoverPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)

    else {
      logger.warn(s"Unable to find a specific kind of play that matches description in ${filename}: ${play}")
      new Event(gameUri, eventNumber, period, time, trimmedPlay)(gamePeriodInfo)
    }

  }

}