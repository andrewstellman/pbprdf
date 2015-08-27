package com.stellmangreene.pbprdf.model

import org.openrdf.model.impl.ValueFactoryImpl
import org.openrdf.model.URI

/**
 * RDF entities
 *
 * @author andrewstellman
 */
object Entities {

  val valueFactory = ValueFactoryImpl.getInstance();

  val NAMESPACE = "http://www.stellman-greene.com/pbprdf/"

  val contextUri = valueFactory.createURI(NAMESPACE, "run_" + System.currentTimeMillis)
  
  /**
   * Generate the URI for a game entity
   */
  def getGameUri(gameId: String): URI = {
    valueFactory.createURI(NAMESPACE, gameId)
  }

  /**
   * Generate the URI for an event entity
   */
  def getEventUri(gameId: String, eventId: Int): URI = {
    valueFactory.createURI(NAMESPACE, s"${gameId}/${eventId.toString}")
  }

}
