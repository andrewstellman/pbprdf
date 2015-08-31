package com.stellmangreene.pbprdf.model

import org.openrdf.model.impl.ValueFactoryImpl
import org.openrdf.model.URI

/**
 * RDF entities
 *
 * @author andrewstellman
 */
object EntityUriFactory {

  private val valueFactory = ValueFactoryImpl.getInstance();

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
  def getEventUri(gameId: String, eventNumber: Int): URI = {
    valueFactory.createURI(NAMESPACE, s"${gameId}/${eventNumber.toString}")
  }
  
  /**
   * Generate the URI for a team
   */
  def getTeamUri(name: String): URI = {
    valueFactory.createURI(NAMESPACE, s"teams/${name.trim.replaceAll(" ", "_")}")
  }
  
  /**
   * Generate the URI for a player
   */
  def getPlayerUri(name: String): URI = {
    valueFactory.createURI(NAMESPACE, s"players/${name.trim.replaceAll(" ", "_")}")
  }
  
}
