package com.stellmangreene.pbprdf

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.vocabulary._
import org.eclipse.rdf4j.repository.Repository

import better.files._

import com.stellmangreene.pbprdf.model.EntityIriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.plays._

import com.stellmangreene.pbprdf.util.RdfOperations._

import com.typesafe.scalalogging.LazyLogging

/**
 * A play-by-play event that can be parsed into RDF triples
 *
 * @param gameIri
 *        IRI of the game
 * @param eventNumber
 *        Sequential number of this event
 * @param period
 *        Period this occurred in (overtime starts with period 5)
 * @param time
 *        Time of the event
 * @param description
 *        Description of the event (eg. "Washington full timeout")
 * @param gamePeriodInfo
 *        Period information
 * @param team
 *        Name of the team (to include in the text file contents for the play-by-play)
 * @param Score
 *        Score for the event (to include in the text file contents for the play-by-play)
 * @param play
 *        Trimmed text of the play (to include in the file contents for the play-by-play)
 *
 * @author andrewstellman
 */
case class Event(gameIri: IRI, eventNumber: Int, period: Int, time: String, description: String)(val gamePeriodInfo: GamePeriodInfo, val team: String, val score: String, play: String)
  extends LazyLogging {

  override def toString = "Period " + period + " " + time + " - " + description

  /** IRI of this event for RDF */
  val eventIri = EntityIriFactory.getEventIri(gameIri, eventNumber)

  /**
   * Add this event to an RDF repository
   *
   * @param rep
   *            rdf4j repository to add the events to
   */
  def addRdf(rep: Repository) = {
    val valueFactory = rep.getValueFactory
    rep.addTriples(eventTriples(valueFactory))
    rep.addTriples(secondsIntoGameTriple(valueFactory))
  }

  /** Generates the type, period, time, and label triples that every event must have */
  private def eventTriples(valueFactory: ValueFactory): Set[(Resource, IRI, Value)] = {
    Set(
      (eventIri, RDF.TYPE, Ontology.EVENT),
      (eventIri, Ontology.IN_GAME, gameIri),
      (eventIri, Ontology.PERIOD, valueFactory.createLiteral(period)),
      (eventIri, Ontology.TIME, valueFactory.createLiteral(time)),
      (eventIri, RDFS.LABEL, valueFactory.createLiteral(description)))
  }

  /** Generate the pbprdf:secondsIntoGame and pbprdf:secondsLeftInPeriod triples */
  private def secondsIntoGameTriple(valueFactory: ValueFactory): Set[(Resource, IRI, Value)] = {
    val secondsLeft = gamePeriodInfo.clockToSecondsLeft(period, time)
    secondsLeft.map(eventTimes => {
      Set[(Resource, IRI, Value)](
        (eventIri, Ontology.SECONDS_INTO_GAME, valueFactory.createLiteral(eventTimes.secondsIntoGame)),
        (eventIri, Ontology.SECONDS_LEFT_IN_PERIOD, valueFactory.createLiteral(eventTimes.secondsLeftInPeriod)))

    })
      .getOrElse(Set[(Resource, IRI, Value)]())
  }

  /** Returns a text description of this event */
  def getText: String = {
    s"$team\t$period\t$time\t$score\t${play.replaceAll("\t", "    ")}"
  }
}

/**
 * Factory to create Play objects, choosing the subclass based on the play description
 *
 * @author andrewstellman
 */
object Event extends LazyLogging {

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
  def apply(gameIri: IRI, filename: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo): Event = {

    val trimmedPlay = play.trim.replaceAll(" +", " ")

    trimmedPlay match {
      case trimmedPlay if BlockPlay.matches(trimmedPlay)                => new BlockPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if DelayOfGamePlay.matches(trimmedPlay)          => new DelayOfGamePlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if EnterPlay.matches(trimmedPlay)                => new EnterPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if FoulPlay.matches(trimmedPlay)                 => new FoulPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if JumpBallPlay.matches(trimmedPlay)             => new JumpBallPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if ReboundPlay.matches(trimmedPlay)              => new ReboundPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if ShotPlay.matches(trimmedPlay)                 => new ShotPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if DoubleTechnicalFoulPlay.matches(trimmedPlay)  => new DoubleTechnicalFoulPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if TechnicalFoulPlay.matches(trimmedPlay)        => new TechnicalFoulPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if ThreeSecondViolationPlay.matches(trimmedPlay) => new ThreeSecondViolationPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if FiveSecondViolationPlay.matches(trimmedPlay)  => new FiveSecondViolationPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if TurnoverPlay.matches(trimmedPlay)             => new TurnoverPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if TimeoutPlay.matches(trimmedPlay)              => new TimeoutPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if EndOfPlay.matches(trimmedPlay)                => new EndOfPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if EjectionPlay.matches(trimmedPlay)             => new EjectionPlay(gameIri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay => {
        logger.warn(s"Could not match play description in ${filename}: ${trimmedPlay}")
        Event(gameIri, eventNumber, period, time, trimmedPlay)(gamePeriodInfo, team, score, play)
      }
    }
  }

  /**
   * Adds pbprdf:nextEvent and pbprdf:previousEvent triples to a list of events
   */
  def addPreviousAndNextTriples(rep: Repository, events: Seq[Event]) = {
    events
      .sortBy(_.eventNumber)
      .zipWithIndex.foreach(e => {
        val (event, index) = e
        rep.addTriple(event.eventIri, Ontology.EVENT_NUMBER, rep.getValueFactory.createLiteral(index + 1))

        if (index + 1 < events.size) {
          val nextEvent = events(index + 1)
          rep.addTriple(event.eventIri, Ontology.NEXT_EVENT, nextEvent.eventIri)

          if (nextEvent.period == event.period) {
            val eventSecondsLeft = event.gamePeriodInfo.clockToSecondsLeft(nextEvent.period, nextEvent.time)
            val nextEventSecondsLeft = event.gamePeriodInfo.clockToSecondsLeft(event.period, event.time)
            if (nextEventSecondsLeft.isDefined && eventSecondsLeft.isDefined) {
              val secondsUntilNextEvent = nextEventSecondsLeft.get.secondsLeftInPeriod - eventSecondsLeft.get.secondsLeftInPeriod
              rep.addTriple(event.eventIri, Ontology.SECONDS_UNTIL_NEXT_EVENT, rep.getValueFactory.createLiteral(secondsUntilNextEvent))
            }
          }
        }

        if (index > 0) {
          val previousEvent = events(index - 1)
          rep.addTriple(event.eventIri, Ontology.PREVIOUS_EVENT, previousEvent.eventIri)

          if (previousEvent.period == event.period) {
            val eventSecondsLeft = event.gamePeriodInfo.clockToSecondsLeft(previousEvent.period, previousEvent.time)
            val previousEventSecondsLeft = event.gamePeriodInfo.clockToSecondsLeft(event.period, event.time)
            if (previousEventSecondsLeft.isDefined && eventSecondsLeft.isDefined) {
              val secondsSincePreviousEvent = eventSecondsLeft.get.secondsLeftInPeriod - previousEventSecondsLeft.get.secondsLeftInPeriod
              rep.addTriple(event.eventIri, Ontology.SECONDS_SINCE_PREVIOUS_EVENT, rep.getValueFactory.createLiteral(secondsSincePreviousEvent))
            }
          }
        }
      })
  }

}
