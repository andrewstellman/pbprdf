package com.stellmangreene.pbprdf

import scala.language.postfixOps
import scala.xml.Elem

import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF
import org.openrdf.repository.Repository

import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.plays.PlayFactory
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.util.XmlHelper
import com.typesafe.scalalogging.LazyLogging

/**
 * @author andrewstellman
 */
class EspnPlayByPlay(gameId: String, rootElem: Elem) extends PlayByPlay with LazyLogging with RdfOperations {

  private val divs = (rootElem \\ "body" \\ "div")

  private val awayTeamElems = XmlHelper.getElemByClassAndTag(divs, "team away", "a")

  /** URI of this game */
  val gameUri: URI = EntityUriFactory.getGameUri(gameId)

  /** Away team name */
  override val awayTeam = XmlHelper.getElemByClassAndTag(divs, "team away", "a").map(_.text).getOrElse("AWAY TEAM NOT FOUND")
  if (awayTeam == "AWAY TEAM NOT FOUND")
    logger.warn("No away team name found")

  /** Away team score */
  override val awayScore = XmlHelper.getElemByClassAndTag(divs, "team away", "span").map(_.text).getOrElse("AWAY SCORE NOT FOUND")
  if (awayScore == "AWAY SCORE NOT FOUND")
    logger.warn("No away team score name found")

  /** Home team name */
  override val homeTeam = XmlHelper.getElemByClassAndTag(divs, "team home", "a").map(_.text).getOrElse("HOME TEAM NOT FOUND")
  if (homeTeam == "HOME TEAM NOT FOUND")
    logger.warn("No home team name found")

  /** Home team score */
  override val homeScore = XmlHelper.getElemByClassAndTag(divs, "team home", "span").map(_.text).getOrElse("HOME SCORE NOT FOUND")
  if (homeScore == "HOME SCORE NOT FOUND")
    logger.warn("No home team score name found")

  private val gameTimeLocationDivs = XmlHelper.getElemByClassAndTag(divs, "game-time-location", "p")
  val timeAndLocation =
    if (gameTimeLocationDivs.isDefined && gameTimeLocationDivs.get.size == 2) {
      (gameTimeLocationDivs.get.head.text, gameTimeLocationDivs.get.tail.text)
    } else {
      ("GAME TIME NOT FOUND", "GAME LOCATION NOT FOUND")
    }

  /** Game time */
  override val gameTime = timeAndLocation._1

  /** Game location */
  override val gameLocation = timeAndLocation._2

  /** Events from the play-by-play */
  override val events: Seq[Event] = readEvents()

  private def readEvents(): Seq[Event] = {
    logger.info("Reading game: " + (awayTeam, awayScore, homeTeam, homeScore).toString)

    val p = (rootElem \\ "table")
      .find(_.attribute("class").mkString == "mod-data mod-pbp")

    val eventsRead = if (p.isEmpty)
      Seq()
    else {
      var period: Int = 0
      var eventNumber: Int = 0

      val eventRows = (p.get \\ "table" \\ "tr")
      eventRows.map(r => {
        if ((r \\ "h4").size > 0) {
          val description = (r \\ "h4" text).mkString
          if (description.endsWith("Summary")) {
            period += 1
            logger.info(s"Reading period ${period}: ${description}")
          }
          None

        } else {
          val td = (r \\ "td")
          if (td.size == 2) {
            eventNumber += 1
            Some(new Event(gameId, eventNumber, period, (td.head text).mkString.trim, (td.last text).mkString.trim))

          } else if (td.size == 4) {
            val time = (td(0) text).mkString
            val score = (td(2) text).mkString
            val awayPlay = (td(1) text).mkString.trim
            val homePlay = (td(3) text).mkString.trim

            val teamNameAndPlay: Option[(String, String)] =
              if (homePlay.size > 1)
                Some((homeTeam, homePlay))
              else if (awayPlay.size > 1)
                Some((awayTeam, awayPlay))
              else
                None

            if (teamNameAndPlay.isDefined) {
              eventNumber += 1
              Some(PlayFactory.createPlay(gameId, eventNumber, period, time, teamNameAndPlay.get._1, teamNameAndPlay.get._2, score))
            } else
              None

          } else
            None
        }
      }).filter(_.isDefined)
        .map(event => {
          logger.debug("Read event: " + event.toString)
          event.get
        })
    }

    if (eventsRead.isEmpty)
      logger.warn(s"No events read")
    logger.info(s"Finished reading game")
    
    eventsRead
  }

  /**
   * Add the events to an RDF repository
   *
   * @param rep
   *            Sesame repository to add the events to
   */
  override def addRdf(rep: Repository) = {
    rep.addTriple(gameUri, RDF.TYPE, Ontology.GAME, EntityUriFactory.contextUri)
    events.foreach(_.addRdf(rep))
    super.addRdf(rep)
  }

  override def toString(): String = {
    s"${awayTeam} at ${homeTeam} on ${gameTime}: ${events.size} events"
  }

}
