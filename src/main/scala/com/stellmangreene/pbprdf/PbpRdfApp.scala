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
import scala.util.Try
import scala.util.Success
import java.io.FileOutputStream

object PbpRdfApp extends App with LazyLogging {

  def printUsageAndExit(message: Option[String] = None) = {
    if (message.isDefined)
      println(message.get)
    println("usage: pbprdf folder [filename.ttl]")
    println("  Read all of the files in the folder and attempt to process them")
    println("  Write all plays for each game to stdout, or file if specified")
    System.exit(1)
  }

  if (args.size != 1 && args.size != 2) {
    printUsageAndExit()
  } else {

    var folder: File = null
    val inputFolderPath = args(0)
    Try(new File(inputFolderPath)) match {
      case Success(f) => { folder = f }
      case _          => printUsageAndExit(Some(s"Unable to open folder: ${inputFolderPath}"))
    }

    if (!folder.exists || !folder.isDirectory)
      printUsageAndExit(Some(s"Invalid folder: ${inputFolderPath}"))

    val files = folder.listFiles
    if (files.isEmpty)
      printUsageAndExit(Some(s"No files found in folder: ${inputFolderPath}"))

    val outputFile =
      if (args.size >= 2) {
        if (new File(args(1)).exists)
          printUsageAndExit(Some(s"File already exists, will not overwrite: ${args(1)}"))
        Some(args(1))

      } else {
        None

      }

    logger.info(s"Reading ${files.size} files from folder ${inputFolderPath}")

    var rep = new SailRepository(new MemoryStore)
    rep.initialize

    var i = 0
    files.foreach(file => {
      i += 1
      logger.info(s"Reading plays from ${file.getName} (file ${i} of ${files.size})")
      val xmlStream = new FileInputStream(file)
      val rootElem = XmlHelper.parseXml(xmlStream)
      val playByPlay: PlayByPlay = new EspnPlayByPlay(file.getName, rootElem)
      playByPlay.addRdf(rep)
    })

    val conn = rep.getConnection
    var statements = conn.getStatements(null, null, null, true)

    var model = Iterations.addAll(statements, new LinkedHashModel)
    model.setNamespace("rdf", RDF.NAMESPACE)
    model.setNamespace("rdfs", RDFS.NAMESPACE)
    model.setNamespace("xsd", XMLSchema.NAMESPACE)
    model.setNamespace("pbprdf", Ontology.NAMESPACE)

    if (outputFile.isDefined) {
      val outputStream = new FileOutputStream(outputFile.get)
      logger.info(s"Writing TriG to ${outputFile.get}")
      Rio.write(model, outputStream, RDFFormat.TRIG)
    } else {
      logger.info("Writing TriG to standard output")
      Rio.write(model, System.out, RDFFormat.TRIG)
    }

    logger.info(s"Finished writing TriG")
  }
}
