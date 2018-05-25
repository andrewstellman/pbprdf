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
import com.stellmangreene.pbprdf.util.RdfOperations
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
  extends RdfOperations with LazyLogging {

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
    rep.addTriples(parseTimeoutTriplesIfPossible(valueFactory))
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

  /**
   * Generate the triples for a timeout event
   * @return Triples for a timeout event, or Set() if this is not a timeout event
   */
  private def parseTimeoutTriplesIfPossible(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val timeoutRegex = """^(.*) (Full|20 Sec\.) timeout$""".r

    description match {
      case timeoutRegex(team, duration) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.TIMEOUT),
          (eventUri, Ontology.TIMEOUT_TEAM, valueFactory.createLiteral(team)),
          (eventUri, Ontology.TIMEOUT_DURATION, valueFactory.createLiteral(duration)))
      }
      case _ => {
        Set()
      }
    }
  }
}

