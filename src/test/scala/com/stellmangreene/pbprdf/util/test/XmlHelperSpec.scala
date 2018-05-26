package com.stellmangreene.pbprdf.util.test

import java.io.FileInputStream
import org.scalatest._
import com.stellmangreene.pbprdf.util.XmlHelper
import scala.language.postfixOps
import better.files._

/**
 * @author andrewstellman
 */
class XmlHelperSpec extends FlatSpec with Matchers {

  behavior of "XmlHelper"

  it should "return the root element with valid XML" in {
    val xml = "src/test/resources/com/stellmangreene/pbprdf/test/htmldata/400610636.html".toFile.newInputStream
    val rootElem = XmlHelper.parseXml(xml)

    ((rootElem \\ "title") text) should be("Washington vs. Connecticut - Play-By-Play - June 5, 2015 - ESPN")

    (rootElem \\ "body" \\ "div")
      .filter(_.attribute("id").isDefined)
      .map(_.attribute("id").get.mkString) should be(
        List("fb-root", "global-viewport", "header-wrapper", "fullbtn", "global-search", "custom-nav", "gamepackage-header-wrap", "gamepackage-matchup-wrap",
          "gamepackage-linescore-wrap", "gamepackage-links-wrap", "gamepackage-wrap", "gamepackage-content-wrap", "gamepackage-column-wrap", "gamepackage-shot-chart",
          "chart1", "accordion-1", "gamepackage-play-by-play", "gamepackage-qtrs-wrap", "gp-quarter-1", "gp-quarter-2", "gp-quarter-3", "gp-quarter-4",
          "gamepackage-outbrain", "gamepackage-shop", "gamepackage-ad", "gamepackage-cliplist", "gamepackage-news", "gamepackage-season-series"))
  }

  it should "get elements by class and tag" in {
    val xml = "src/test/resources/com/stellmangreene/pbprdf/test/htmldata/400610636-gameinfo.html".toFile.newInputStream
    val rootElem = XmlHelper.parseXml(xml)

    val divs = (rootElem \\ "body" \\ "div")
    val gameTimeLocationDivs = XmlHelper.getElemByClassAndTag(divs, "game-date-time", "span")
    gameTimeLocationDivs.get.size should be(3)
    gameTimeLocationDivs.get.mkString.contains("""<span data-behavior="date_time" data-date="2015-06-05T23:00Z" xmlns="http://www.w3.org/1999/xhtml">""") should be(true)
  }

}
