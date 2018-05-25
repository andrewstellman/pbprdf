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

  val xml = "src/test/resources/com/stellmangreene/pbprdf/test/htmldata/400610636.html".toFile.newInputStream
  val rootElem = XmlHelper.parseXml(xml)

  it should "return the root element with valid XML" in {
    ((rootElem \\ "title") text) should be("Washington Mystics vs. Connecticut Sun - Play By Play - June 05, 2015 - ESPN")

    (rootElem \\ "body" \\ "div")
      .filter(_.attribute("id").isDefined)
      .map(_.attribute("id").get.mkString) should be(
        List(
          "subheader", "content-wrapper", "ad-top", "content", "fb-root", "ootScoreboard", "ootShadow", "scoreboard2", "wnba-scores",
          "gamepackageTop", "matchup-wnba-400610636", "gp-adwrap", "global-viewport", "header-wrapper", "global-search", "footer"))

  }

  it should "get elements by class and tag" in {
    val divs = (rootElem \\ "body" \\ "div")
    val gameTimeLocationDivs = XmlHelper.getElemByClassAndTag(divs, "game-time-location", "p")
    gameTimeLocationDivs.get.size should be(2)
    gameTimeLocationDivs.get.head.mkString should be("""<p xmlns="http://www.w3.org/1999/xhtml">7:00 PM ET, June 5, 2015</p>""")
    gameTimeLocationDivs.get.tail.mkString should be("""<p xmlns="http://www.w3.org/1999/xhtml">Mohegan Sun Arena, Uncasville, CT</p>""")
  }

}
