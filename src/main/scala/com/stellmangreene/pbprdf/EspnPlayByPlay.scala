package com.stellmangreene.pbprdf

import scala.language.postfixOps
import scala.util.Success
import scala.util.Try
import scala.xml.Elem
import org.joda.time.format.DateTimeFormat
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.repository.Repository
import com.stellmangreene.pbprdf.model.EntityUriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.plays.PlayFactory
import com.stellmangreene.pbprdf.util.RdfOperations
import com.stellmangreene.pbprdf.util.XmlHelper
import com.typesafe.scalalogging.LazyLogging
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import scala.util.Failure
import org.joda.time.DateTime

/**
 * @author andrewstellman
 */
class EspnPlayByPlay(rootElem: Elem, filename: String) extends PlayByPlay with LazyLogging with RdfOperations {

  private val divs = (rootElem \\ "body" \\ "div")

  private val awayTeamElems = XmlHelper.getElemByClassAndTag(divs, "team away", "a")

  private def getElementFromXml(clazz: String, tag: String): String = {
    Try(XmlHelper.getElemByClassAndTag(divs, clazz, tag).map(_.text).get) match {
      case Success(s) => s
      case _ => {
        val msg = s"Unable to find ${clazz} in ${filename}"
        logger.error(msg)
        throw new InvalidPlayByPlayException(msg)
      }
    }
  }

  /** Away team name */
  override val awayTeam = getElementFromXml("team away", "a")

  /** Away team score */
  override val awayScore = getElementFromXml("team away", "span")

  /** Home team name */
  override val homeTeam = getElementFromXml("team home", "a")

  /** Home team score */
  override val homeScore = getElementFromXml("team home", "span")

  private val gameTimeLocationDivs = XmlHelper.getElemByClassAndTag(divs, "game-time-location", "p")
  val timeAndLocation =
    if (gameTimeLocationDivs.isDefined && gameTimeLocationDivs.get.size == 2) {
      (gameTimeLocationDivs.get.head.text, gameTimeLocationDivs.get.tail.text)
    } else {
        val msg = s"Unable to find game time and location in ${filename}"
        logger.error(msg)
        throw new InvalidPlayByPlayException(msg)
    }

  /** Game time */
  val dateTimeFormatter = DateTimeFormat.forPattern("h:m a 'ET', MMM d, y")
  override val gameTime: DateTime =
    Try(dateTimeFormatter.parseDateTime(timeAndLocation._1)) match {
      case Success(dateTime) => {
        dateTime
      }
      case Failure(e: Throwable) => {
        val message = s"Unable to parse game time in ${filename}: ${e.getMessage}"
        logger.error(message)
        throw new InvalidPlayByPlayException(message)
      }
    }

  /** Game location */
  override val gameLocation = timeAndLocation._2

  /** URI of this game */
  val gameUri: URI = EntityUriFactory.getGameUri(homeTeam, awayTeam, gameTime)

  /** Events from the play-by-play */
  override val events: Seq[Event] = readEvents()

  private def readEvents(): Seq[Event] = {
    logger.debug("Reading game: " + (awayTeam, awayScore, homeTeam, homeScore).toString)

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
            logger.debug(s"Reading period ${period}: ${description}")
          }
          None

        } else {
          val td = (r \\ "td")
          if (td.size == 2) {
            eventNumber += 1
            Some(new Event(gameUri, eventNumber, period, (td.head text).mkString.trim, (td.last text).mkString.trim))

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
              Some(PlayFactory.createPlay(gameUri, eventNumber, period, time, teamNameAndPlay.get._1, teamNameAndPlay.get._2, score))
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
      logger.warn(s"No events read from ${filename}")
    logger.debug(s"Finished reading game")

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
    rep.addTriple(gameUri, Ontology.GAME_LOCATION, rep.getValueFactory.createLiteral(gameLocation), EntityUriFactory.contextUri)
    rep.addTriple(gameUri, RDFS.LABEL, rep.getValueFactory.createLiteral(this.toString), EntityUriFactory.contextUri)
    val gregorianGameTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(gameTime.toGregorianCalendar())
    rep.addTriple(gameUri, Ontology.GAME_TIME, rep.getValueFactory.createLiteral(gregorianGameTime), EntityUriFactory.contextUri)
    
    events.foreach(_.addRdf(rep))
    super.addRdf(rep)
  }

  override def toString(): String = {
    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
    s"${awayTeam} (${awayScore}) at ${homeTeam} (${homeScore}) on ${fmt.print(gameTime)}: ${events.size} events"
  }

}
