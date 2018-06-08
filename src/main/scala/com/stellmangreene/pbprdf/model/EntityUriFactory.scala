package com.stellmangreene.pbprdf.model

import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.URI
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * RDF entities
 *
 * @author andrewstellman
 */
object EntityUriFactory {

  private val valueFactory = SimpleValueFactory.getInstance()

  val NAMESPACE = "http://stellman-greene.com/pbprdf/"

  /**
   * Generate the URI for a game entity
   */
  def getGameUri(homeTeam: String, awayTeam: String, gameTime: DateTime): URI = {
    valueFactory.createURI(NAMESPACE, s"games/${getGameIdentifierString(homeTeam, awayTeam, gameTime)}")
  }

  def getGameIdentifierString(homeTeam: String, awayTeam: String, gameTime: DateTime): String = {
    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
    s"${fmt.print(gameTime)}_${awayTeam.trim.replaceAll(" ", "_")}_at_${homeTeam.trim.replaceAll(" ", "_")}"
  }

  /**
   * Generate the URI for an event entity
   */
  def getEventUri(gameUri: URI, eventNumber: Int): URI = {
    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
    valueFactory.createURI(s"${gameUri.stringValue}/${eventNumber.toString}")
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
