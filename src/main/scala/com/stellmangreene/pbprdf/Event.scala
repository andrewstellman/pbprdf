package com.stellmangreene.pbprdf

import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.util.RdfOperations
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.vocabulary.RDF
import com.stellmangreene.pbprdf.model.Entities
import com.stellmangreene.pbprdf.model.Ontology
import org.openrdf.model.Value
import org.openrdf.model.URI
import org.openrdf.model.Resource
import org.openrdf.model.ValueFactory
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
case class Event(gameId: String, eventNumber: Int, period: Int, time: String, description: String)
    extends RdfOperations with LazyLogging {

  override def toString = "Period " + period + " " + time + " - " + description

  val eventUri = Entities.valueFactory.createURI(Entities.NAMESPACE, s"${gameId}/${eventNumber.toString}")

  def addRdf(rep: Repository) = {
    val triples: Set[(Resource, URI, Value)] = generateTriples(rep.getValueFactory)
    rep.addTriples(triples, Entities.contextUri)
  }

  private def generateTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    Set(
      (eventUri, RDF.TYPE, Ontology.EVENT),
      (eventUri, Ontology.PERIOD, valueFactory.createLiteral(period)),
      (eventUri, Ontology.TIME, valueFactory.createLiteral(time)),
      (eventUri, RDFS.LABEL, valueFactory.createLiteral(description))) ++
      secondsIntoGameTriple(valueFactory) ++
      parseTimeoutTriples(valueFactory)
  }

  private def secondsIntoGameTriple(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val timeRegex = """^(\d+):(\d+)$""".r

    time match {
      case timeRegex(minutes, seconds) => {
        val secondsIntoGame: Int =
          if (period <= 4)
            if (time == "10:00")
              (period - 1) * 10 * 60
            else
              (period - 1) * 10 * 60 +
                (9 - minutes.toInt) * 60 +
                (60 - seconds.toInt)

          // Overtime
          else if (time == "5:00")
            40 * 60 + (period - 5) * 5 * 60
          else
            40 * 60 +
              (period - 5) * 5 * 60 +
              (4 - minutes.toInt) * 60 +
              (60 - seconds.toInt)

        Set((eventUri, Ontology.SECONDS_INTO_GAME, valueFactory.createLiteral(secondsIntoGame)))
      }
      case _ => {
        logger.warn(s"Unable to calculate seconds into game from time ${time}")
        Set()
      }
    }
  }

  private def parseTimeoutTriples(valueFactory: ValueFactory): Set[(Resource, URI, Value)] = {
    val regex = """^(.*) (Full|20 Sec\.) timeout$""".r

    description match {
      case regex(team, duration) => {
        Set(
          (eventUri, RDF.TYPE, Ontology.TIMEOUT),
          (eventUri, Ontology.TEAM, valueFactory.createLiteral(team)),
          (eventUri, Ontology.TIMEOUT_DURATION, valueFactory.createLiteral(duration)))
      }
      case _ => {
        Set()
      }
    }
  }
}

