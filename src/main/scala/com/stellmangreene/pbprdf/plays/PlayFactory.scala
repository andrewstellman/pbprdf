package com.stellmangreene.pbprdf.plays

import com.stellmangreene.pbprdf.Event
import com.stellmangreene.pbprdf.util.RdfOperations
import com.typesafe.scalalogging.LazyLogging

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
   *
   * @author andrewstellman
   */
  def createPlay(gameId: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String): Event = {

    if (BlockPlay.matches(play))
      new BlockPlay(gameId, eventNumber, period, time, team, play, score)

    else if (DelayOfGamePlay.matches(play))
      new DelayOfGamePlay(gameId, eventNumber, period, time, team, play, score)

    else if (EnterPlay.matches(play))
      new EnterPlay(gameId, eventNumber, period, time, team, play, score)

    else if (FoulPlay.matches(play))
      new FoulPlay(gameId, eventNumber, period, time, team, play, score)

    else if (JumpBallPlay.matches(play))
      new JumpBallPlay(gameId, eventNumber, period, time, team, play, score)

    else if (ReboundPlay.matches(play))
      new ReboundPlay(gameId, eventNumber, period, time, team, play, score)

    else if (ShotPlay.matches(play))
      new ShotPlay(gameId, eventNumber, period, time, team, play, score)

    else if (TechnicalFoulPlay.matches(play))
      new TechnicalFoulPlay(gameId, eventNumber, period, time, team, play, score)

    else if (ThreeSecondViolationPlay.matches(play))
      new ThreeSecondViolationPlay(gameId, eventNumber, period, time, team, play, score)

    else if (TurnoverPlay.matches(play))
      new TurnoverPlay(gameId, eventNumber, period, time, team, play, score)

    else {
      logger.warn(s"Unable to find a specific kind of play that matches description: ${play}")
      new Event(gameId, eventNumber, period, time, play)
    }

  }

}