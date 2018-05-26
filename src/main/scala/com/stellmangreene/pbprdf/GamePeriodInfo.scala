package com.stellmangreene.pbprdf

import com.typesafe.scalalogging.LazyLogging

/** Times for a specific event */
case class EventTimes(secondsIntoGame: Int, secondsLeftInPeriod: Int)

/** Companion object for GamePeriodInfo that defines period information for various leagues */
object GamePeriodInfo {
  val WNBAPeriodInfo = GamePeriodInfo(10, 5, 4)
  val NBAPeriodInfo = GamePeriodInfo(12, 5, 0)
  val NCAAWPeriodInfo = GamePeriodInfo(10, 5, 4)
  val NCAAMPeriodInfo = GamePeriodInfo(20, 5, 2)
}

/** Information about the number of periods in the game and the number of minutes in each period */
case class GamePeriodInfo(regulationPeriodMinutes: Int, overtimePeriodMinutes: Int, regulationPeriods: Int) extends LazyLogging {

  /**
   * converts a clock reading into seconds into the game and left in period
   *
   * @param period
   *        the period of the game
   * @param time
   *        a clock reading (e.g. "9:47", "37.6")
   * @return a tuple of (seconds into the game, seconds left in period)
   */
  def clockToSecondsLeft(period: Int, time: String): Option[EventTimes] = {

    val timeRegex = """^(\d+):(\d+)$""".r
    val secondsRegex = """^(\d+).(\d+)$""".r

    val result =
      time match {
        case timeRegex(minutes, seconds) => {
          Some((minutes.toInt, seconds.toInt, 0))
        }
        case secondsRegex(seconds, fraction) => {
          Some((0, seconds.toInt, fraction.toInt))
        }
        case _ => {
          logger.warn(s"Unable to calculate seconds into game from time ${time}")
          None
        }
      }

    result.map(e => {
      val (minutes, seconds, fraction) = e

      val inOvertime = (period > regulationPeriods)

      val secondsIntoPeriod =
        inOvertime match {
          case false if (minutes == regulationPeriodMinutes) => 0 // 10:00 in a WNBA game means zero seconds have elapsed
          case false => ((regulationPeriodMinutes - 1 - minutes) * 60) + (60 - seconds)
          case true if (minutes == overtimePeriodMinutes) => 0
          case true => ((overtimePeriodMinutes - 1 - minutes) * 60) + (60 - seconds)
        }

      val previousRegulationPeriodSecondsElapsed =
        if (inOvertime) regulationPeriods * regulationPeriodMinutes * 60
        else (period - 1) * regulationPeriodMinutes * 60

      val previousOvertimePeriodSecondsElapsed =
        if (inOvertime) (period - regulationPeriods - 1) * overtimePeriodMinutes * 60
        else 0

      val secondsIntoGame = secondsIntoPeriod + previousRegulationPeriodSecondsElapsed + previousOvertimePeriodSecondsElapsed
      
      val secondsLeftInPeriod =
        if (inOvertime) (overtimePeriodMinutes * 60) - secondsIntoPeriod
        else (regulationPeriodMinutes * 60) - secondsIntoPeriod

      EventTimes(secondsIntoGame, secondsLeftInPeriod)
    })

  }
}
