package com.stellmangreene.pbprdf

import org.joda.time.DateTime
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.repository.Repository

import com.stellmangreene.pbprdf.model.EntityIriFactory
import com.stellmangreene.pbprdf.model.Ontology
import com.stellmangreene.pbprdf.plays.EnterPlay

import com.stellmangreene.pbprdf.util.RdfOperations._

import better.files._

import com.typesafe.scalalogging.LazyLogging
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.DateTimeFormat
import javax.xml.datatype.DatatypeFactory

//TODO: Add triples for the current score (e.g. "10-4") -- grep -r "CURRENTLY IGNORED" src/main/scala
//TODO: Add triples for the box score, test against official box scores
//TODO: Add triples for the players on the court for each possession

/**
 * Play by play that can generate RDF and contents of a text file
 *
 * @author andrewstellman
 */
abstract class PlayByPlay extends LazyLogging {

  /** Events from the play-by-play */
  val events: Seq[Event]

  /** IRI of this game */
  val gameIri: IRI

  /** Name of the home team */
  val homeTeam: String

  /** Final score for the home team */
  val homeScore: String

  /** Name of the away team */
  val awayTeam: String

  /** Final score for the away team */
  val awayScore: String

  /** Game location */
  val gameLocation: Option[String]

  /** Game time */
  val gameTime: DateTime

  /** Game source (eg. filename) */
  val gameSource: String

  /** Game period information */
  val gamePeriodInfo: GamePeriodInfo

  /** returns the league (e.g. Some("WNBA")) based on GamePeriodInfo, None if unrecognized */
  def league = {
    gamePeriodInfo match {
      case GamePeriodInfo.WNBAPeriodInfo  => Some("WNBA")
      case GamePeriodInfo.NBAPeriodInfo   => Some("NBA")
      case GamePeriodInfo.NCAAWPeriodInfo => Some("NCAAW")
      case GamePeriodInfo.NCAAMPeriodInfo => Some("NCAAM")
      case _ => {
        logger.warn("Unrecognized league")
        None
      }
    }
  }

  /**
   * Add the events to an RDF repository
   *
   * @param rep
   *            rdf4j repository to add the events to
   */
  def addRdf(rep: Repository) = {
    rep.addTriple(gameIri, RDF.TYPE, Ontology.GAME)
    gameLocation.foreach(location =>
      rep.addTriple(gameIri, Ontology.GAME_LOCATION, rep.getValueFactory.createLiteral(location)))
    rep.addTriple(gameIri, RDFS.LABEL, rep.getValueFactory.createLiteral(this.toString))
    val gregorianGameTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(gameTime.toGregorianCalendar())
    rep.addTriple(gameIri, Ontology.GAME_TIME, rep.getValueFactory.createLiteral(gregorianGameTime))
    events.foreach(_.addRdf(rep))
    Event.addPreviousAndNextTriples(rep, events)
    addRosterBnodes(rep)
  }

  /**
   * Use the "player enters" events to build the home and away team rosters and
   * add a bnode for each roster
   *
   * @param rep
   *            rdf4j repository to add the events to
   */
  protected def addRosterBnodes(rep: Repository) = {

    val homeTeamRosterBnode = rep.getValueFactory.createBNode
    val awayTeamRosterBnode = rep.getValueFactory.createBNode

    rep.addTriple(EntityIriFactory.getTeamIri(homeTeam), RDF.TYPE, Ontology.TEAM)
    rep.addTriple(gameIri, Ontology.HOME_TEAM, EntityIriFactory.getTeamIri(homeTeam))
    rep.addTriple(gameIri, Ontology.HAS_HOME_TEAM_ROSTER, homeTeamRosterBnode)
    rep.addTriple(homeTeamRosterBnode, RDF.TYPE, Ontology.ROSTER)
    rep.addTriple(homeTeamRosterBnode, Ontology.ROSTER_TEAM, EntityIriFactory.getTeamIri(homeTeam))
    rep.addTriple(homeTeamRosterBnode, RDFS.LABEL, rep.getValueFactory.createLiteral(homeTeam))

    rep.addTriple(EntityIriFactory.getTeamIri(awayTeam), RDF.TYPE, Ontology.TEAM)
    rep.addTriple(gameIri, Ontology.AWAY_TEAM, EntityIriFactory.getTeamIri(awayTeam))
    rep.addTriple(gameIri, Ontology.HAS_AWAY_TEAM_ROSTER, awayTeamRosterBnode)
    rep.addTriple(awayTeamRosterBnode, RDF.TYPE, Ontology.ROSTER)
    rep.addTriple(awayTeamRosterBnode, Ontology.ROSTER_TEAM, EntityIriFactory.getTeamIri(awayTeam))
    rep.addTriple(awayTeamRosterBnode, RDFS.LABEL, rep.getValueFactory.createLiteral(awayTeam))

    val playerTeamMap: Map[String, String] = events
      .filter(_.isInstanceOf[EnterPlay])
      .map(_.asInstanceOf[EnterPlay])
      .filter(_.playerEntering.isDefined)
      .map(enterPlay => enterPlay.playerEntering.get -> enterPlay.getTeam)
      .toMap

    val teams = playerTeamMap.values.toSeq.distinct
    if (teams.size != 2)
      logger.warn(s"Found entry plays with invalid number of teams ${teams.size} for game <${gameIri}> in ${gameSource}")

    val players = playerTeamMap.keys.toSeq.distinct
    players.foreach(player => {
      rep.addTriple(EntityIriFactory.getPlayerIri(player), RDFS.LABEL, rep.getValueFactory.createLiteral(player.trim))

      val playerTeam = playerTeamMap.get(player).get
      val playerIri = EntityIriFactory.getPlayerIri(player)
      rep.addTriple(playerIri, RDF.TYPE, Ontology.PLAYER)
      if (playerTeam == homeTeam) {
        rep.addTriple(homeTeamRosterBnode, Ontology.HAS_PLAYER, playerIri)
      } else if (playerTeam == awayTeam) {
        rep.addTriple(awayTeamRosterBnode, Ontology.HAS_PLAYER, playerIri)
      } else {
        logger.warn(s"Entry plays contain team ${playerTeam} that does match home team ${homeTeam} or away team ${awayTeam} in ${gameSource}")
      }
    })
  }

  /**
   * returns the contents of a text file representation of this play-by-play, or None if the play can't be rendered correctly
   */
  def textFileContents: Option[Seq[String]] = {
    val header = Seq(
      toString,
      s"${gameLocation.getOrElse("Unknown Location")}\t${ISODateTimeFormat.dateTime().print(gameTime)}")

    val eventLines = events.map(_.getText)

    Some(header ++ eventLines)
  }

  override def toString: String = {
    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
    val s = s"${awayTeam} (${awayScore}) at ${homeTeam} (${homeScore}) on ${fmt.print(gameTime)}"
    if (events.isEmpty) s"Empty Game: $s"
    else {
      s"${league.getOrElse("Unrecognized league")} game: $s - ${events.size} events"
    }
  }

}