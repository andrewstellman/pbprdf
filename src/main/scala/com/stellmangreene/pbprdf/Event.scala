package com.stellmangreene.pbprdf

import org.openrdf.model.Resource
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.openrdf.model.ValueFactory
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.repository.Repository

import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.plays._

import com.stellmangreene.pbprdf.util.RdfOperations._

import com.typesafe.scalalogging.LazyLogging

/**
 * A play-by-play event that can be parsed into RDF triples
 *
 * @param gameID
 *        Unique ID of the game
 * @param eventNumber
 *        Sequential number of this event
 * @param period
 *        Period this occurred in (overtime starts with period 5)
 * @param team
 *        Name of the team
 * @param description
 *        Description of the event (eg. "Washington full timeout")
 *
 * @author andrewstellman
 */
case class Event(gameUri: URI, eventNumber: Int, period: Int, time: String, description: String)(gamePeriodInfo: GamePeriodInfo)
  extends LazyLogging {

  override def toString = "Period " + period + " " + time + " - " + description

  /** URI of this event for RDF */
  val eventUri = EntityUriFactory.getEventUri(gameUri, eventNumber)

  /**
   * Add this event to an RDF repository
   *
   * @param rep
   *            Sesame repository to add the events to
   */
  def addRdf(rep: Repository) = {
    val valueFactory = rep.getValueFactory
    rep.addTriples(eventTriples(valueFactory))
    rep.addTriples(secondsIntoGameTriple(valueFactory))
  }

  /** Generate the type, period, time, and label triples that every event must have */
  private def eventTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    Set(
      (eventUri, RDF.TYPE, Ontology.EVENT),
      (eventUri, Ontology.IN_GAME, gameUri),
      (eventUri, Ontology.PERIOD, valueFactory.createLiteral(period)),
      (eventUri, Ontology.TIME, valueFactory.createLiteral(time)),
      (eventUri, RDFS.LABEL, valueFactory.createLiteral(description)))
  }

  /** Generate the pbprdf:secondsIntoGame and pbprdf:secondsLeftInPeriod triples */
  private def secondsIntoGameTriple(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val secondsLeft = gamePeriodInfo.clockToSecondsLeft(period, time)
    secondsLeft.map(eventTimes => {
      Set[(Resource, URI, Value)](
        (eventUri, Ontology.SECONDS_INTO_GAME, valueFactory.createLiteral(eventTimes.secondsIntoGame)),
        (eventUri, Ontology.SECONDS_LEFT_IN_PERIOD, valueFactory.createLiteral(eventTimes.secondsLeftInPeriod)))

    })
      .getOrElse(Set[(Resource, URI, Value)]())
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
  def apply(gameUri: URI, filename: String, eventNumber: Int, period: Int, time: String, team: String, play: String, score: String, gamePeriodInfo: GamePeriodInfo): Event = {

    val trimmedPlay = play.trim.replaceAll(" +", " ")

    trimmedPlay match {
      case trimmedPlay if BlockPlay.matches(trimmedPlay)                => new BlockPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if DelayOfGamePlay.matches(trimmedPlay)          => new DelayOfGamePlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if EnterPlay.matches(trimmedPlay)                => new EnterPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if FoulPlay.matches(trimmedPlay)                 => new FoulPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if JumpBallPlay.matches(trimmedPlay)             => new JumpBallPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if ReboundPlay.matches(trimmedPlay)              => new ReboundPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if ShotPlay.matches(trimmedPlay)                 => new ShotPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if DoubleTechnicalFoulPlay.matches(trimmedPlay)  => new DoubleTechnicalFoulPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if TechnicalFoulPlay.matches(trimmedPlay)        => new TechnicalFoulPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if ThreeSecondViolationPlay.matches(trimmedPlay) => new ThreeSecondViolationPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if FiveSecondViolationPlay.matches(trimmedPlay)  => new FiveSecondViolationPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if TurnoverPlay.matches(trimmedPlay)             => new TurnoverPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if TimeoutPlay.matches(trimmedPlay)              => new TimeoutPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if EndOfPlay.matches(trimmedPlay)                => new EndOfPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay if EjectionPlay.matches(trimmedPlay)             => new EjectionPlay(gameUri, eventNumber, period, time, team, trimmedPlay, score, gamePeriodInfo)
      case trimmedPlay => {
        logger.warn(s"Could not match play description in ${filename}: ${trimmedPlay}")
        new Event(gameUri, eventNumber, period, time, trimmedPlay)(gamePeriodInfo)
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
        if (index + 1 < events.size) {
          rep.addTriple(event.eventUri, Ontology.NEXT_EVENT, events(index + 1).eventUri)
        }
        if (index > 0) {
          rep.addTriple(event.eventUri, Ontology.PREVIOUS_EVENT, events(index - 1).eventUri)
        }
      })
  }

}
