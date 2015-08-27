package com.stellmangreene.pbprdf

import scala.xml.Elem
import scala.xml.NodeSeq
import com.typesafe.scalalogging.LazyLogging
import scala.language.postfixOps
import org.openrdf.repository.sail.SailRepository
import org.openrdf.model.vocabulary.RDF
import com.stellmangreene.pbprdf.model.Entities
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.util.XmlHelper
import com.stellmangreene.pbprdf.util.RdfOperations

/**
 * @author andrewstellman
 */
class EspnPlayByPlay(gameId: String, rootElem: Elem) extends LazyLogging with RdfOperations {

  private val divs = (rootElem \\ "body" \\ "div")

  private val awayTeamElems = XmlHelper.getElemByClassAndTag(divs, "team away", "a")

  /** Away team name */
  val awayTeam = XmlHelper.getElemByClassAndTag(divs, "team away", "a").map(_.text)
  if (awayTeam.isEmpty)
    logger.warn("No away team name found")

  /** Away team score */
  val awayScore = XmlHelper.getElemByClassAndTag(divs, "team away", "span").map(_.text)

  /** Home team name */
  val homeTeam = XmlHelper.getElemByClassAndTag(divs, "team home", "a").map(_.text)
  if (homeTeam.isEmpty)
    logger.warn("No home team name found")

  /** Home team score */
  val homeScore = XmlHelper.getElemByClassAndTag(divs, "team home", "span").map(_.text)

  private val gameTimeLocationDivs = XmlHelper.getElemByClassAndTag(divs, "game-time-location", "p")
  val (
    /** Game time */
    gameTime,
    /** Game location */
    gameLocation) =
    if (gameTimeLocationDivs.isDefined && gameTimeLocationDivs.get.size == 2) {
      (Some(gameTimeLocationDivs.get.head.text), Some(gameTimeLocationDivs.get.tail.text))
    } else {
      (None, None)
    }

  logger.info("Reading game: " + (awayTeam, awayScore, homeTeam, homeScore).toString)

  private def readEvents(): Seq[Event] = {
    val p = (rootElem \\ "table")
      .find(_.attribute("class").mkString == "mod-data mod-pbp")

    if (p.isEmpty)
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
            
            if (homePlay.size > 1) {
              eventNumber += 1
              Some(new Play(gameId, eventNumber, period, time, homeTeam.getOrElse("HOME TEAM"), homePlay, score))
            } else if (awayPlay.size > 1) {
              eventNumber += 1
              Some(new Play(gameId, eventNumber, period, time, awayTeam.getOrElse("AWAY TEAM"), awayPlay, score))
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
  }

  /** Events from the play-by-play */
  val events: Seq[Event] = readEvents()

  logger.info(s"Finished reading game ${this}")

  def addRdf(rep: SailRepository) = {
    rep.addTriple(Entities.getGameUri("400610636"), RDF.TYPE, Ontology.GAME, Entities.contextUri)
    events.foreach(_.addRdf(rep))
  }

  override def toString(): String = {
    s"${awayTeam.getOrElse("(away team name not found)")} at ${homeTeam.getOrElse("(home team name not found)")} on ${gameTime.getOrElse("(game time not found)")}: ${events.size} events"
  }

}
