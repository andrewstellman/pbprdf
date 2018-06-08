package com.stellmangreene.pbprdf.model

import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.IRI
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * RDF entities
 *
 * @author andrewstellman
 */
object EntityIriFactory {

  private val valueFactory = SimpleValueFactory.getInstance()

  val NAMESPACE = "http://stellman-greene.com/pbprdf/"

  /**
   * Generate the IRI for a game entity
   */
  def getGameIri(homeTeam: String, awayTeam: String, gameTime: DateTime): IRI = {
    valueFactory.createIRI(NAMESPACE, s"games/${getGameIdentifierString(homeTeam, awayTeam, gameTime)}")
  }

  def getGameIdentifierString(homeTeam: String, awayTeam: String, gameTime: DateTime): String = {
    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
    s"${fmt.print(gameTime)}_${awayTeam.trim.replaceAll(" ", "_")}_at_${homeTeam.trim.replaceAll(" ", "_")}"
  }

  /**
   * Generate the IRI for an event entity
   */
  def getEventIri(gameIri: IRI, eventNumber: Int): IRI = {
    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
    valueFactory.createIRI(s"${gameIri.stringValue}/${eventNumber.toString}")
  }

  /**
   * Generate the IRI for a team
   */
  def getTeamIri(name: String): IRI = {
    valueFactory.createIRI(NAMESPACE, s"teams/${name.trim.replaceAll(" ", "_")}")
  }

  /**
   * Generate the IRI for a player
   */
  def getPlayerIri(name: String): IRI = {
    valueFactory.createIRI(NAMESPACE, s"players/${name.trim.replaceAll(" ", "_")}")
  }

}
