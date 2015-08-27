package com.stellmangreene.pbprdf

import org.openrdf.repository.Repository
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.model.vocabulary.FOAF
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.vocabulary.XMLSchema
import org.openrdf.model.impl.LinkedHashModel
import info.aduna.iteration.Iterations
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.Rio
import com.typesafe.scalalogging.LazyLogging
import java.io.FileInputStream
import java.io.File
import org.xml.sax.InputSource
import com.stellmangreene.pbprdf.model._
import com.stellmangreene.pbprdf.util.XmlHelper

object PbpRdfApp extends App with LazyLogging {

  logger.info("Reading HTML play by play")
  
  val INPUT = "src/test/resources/com/stellmangreene/pbprdf/test/htmldata/400610636.html"

  val xmlStream = new FileInputStream(INPUT)
  val rootElem = XmlHelper.parseXml(xmlStream)
  val playByPlay = new EspnPlayByPlay("400610636", rootElem)

  var rep = new SailRepository(new MemoryStore)
  rep.initialize

  logger.info("Generating triples from unit test play by play")
  playByPlay.addRdf(rep)

  logger.info("Writing Turtle")
  
  val conn = rep.getConnection
  var statements = conn.getStatements(null, null, null, true)

  var model = Iterations.addAll(statements, new LinkedHashModel)
  model.setNamespace("rdf", RDF.NAMESPACE)
  model.setNamespace("rdfs", RDFS.NAMESPACE)
  model.setNamespace("xsd", XMLSchema.NAMESPACE)
  model.setNamespace("pbprdf", Ontology.NAMESPACE)
  Rio.write(model, System.out, RDFFormat.TURTLE)
}
