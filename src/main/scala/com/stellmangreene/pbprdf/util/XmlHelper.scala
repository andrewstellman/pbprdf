package com.stellmangreene.pbprdf.util

import java.io.InputStream
import java.io.StringWriter
import scala.xml.XML
import org.ccil.cowan.tagsoup.Parser
import org.ccil.cowan.tagsoup.XMLWriter
import org.xml.sax.InputSource
import scala.xml.Elem
import scala.xml.NodeSeq

/**
 * Helper object to parse XML
 * <p>
 * Uses tagsoup to parse HTML from ESPN.com that breaks scala.xml.XML by default
 * See also http://scala-language.1934581.n4.nabble.com/How-to-use-TagSoup-with-Scala-XML-td1940874.html
 *
 * @author andrewstellman
 */
object XmlHelper {

  /**
   * Parse the root element from an InputStream with XML
   * @param xmlStream     InputStream with the XML data
   * @return              Scala XML Elem element
   */
  def parseXml(xmlStream: InputStream): Elem = {
    val parser = new Parser()
    val writer = new StringWriter()

    parser.setContentHandler(new XMLWriter(writer))
    val source = new InputSource(xmlStream)

    parser.parse(source)

    parser.setContentHandler(new XMLWriter(writer))
    XML.loadString(writer.toString())
  }

  /**
   * Get children of an element by class and tag
   * @param clazz     Class to look for
   * @param tag       Tag to look for
   * @return          Some(NodeSeq) with the matching nodes, or None if not found 
   */
  def getElemByClassAndTag(elem: NodeSeq, clazz: String, tag: String): Option[NodeSeq] = {
    elem
      .find(_.attribute("class").mkString == clazz)
      .map(_ \\ tag)
  }

}