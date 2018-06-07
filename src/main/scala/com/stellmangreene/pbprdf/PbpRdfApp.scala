package com.stellmangreene.pbprdf

import java.io.File

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore

import com.stellmangreene.pbprdf.model.OntologyRdfRepository

import com.stellmangreene.pbprdf.util.RdfOperations._

import better.files._
import org.joda.time._
import org.joda.time.format._

import com.typesafe.scalalogging.LazyLogging
import com.stellmangreene.pbprdf.model.OntologyRdfRepository
import com.stellmangreene.pbprdf.model.EntityUriFactory
import org.joda.time.format.DateTimeFormat

//TODO: Migrate to rdf4j - search comments for "Sesame"
//TODO: Remove "www." from base IRI

object PbpRdfApp extends App with LazyLogging {

  def printUsageAndExit(message: Option[String] = None) = {
    if (message.isDefined)
      println(message.get)
    println("""usage: pbprdf folder [filename.ttl]
  Read all of the files in the folder and attempt to process them
  Write all plays for each game to stdout, or a file if specified
  
pbprdf --ontology [filename.ttl]
  Write the ontology to stout, or a file if specified""")
    System.exit(0)
  }

  if (args.size != 1 && args.size != 2) {
    printUsageAndExit()
  } else {

    val outputFile =
      if (args.size >= 2) {
        if (new File(args(1)).exists)
          printUsageAndExit(Some(s"File already exists, will not overwrite: ${args(1)}"))
        Some(args(1))

      } else {
        None

      }

    if (args(0) == "--ontology") {

      logger.info("Writing ontology statements")
      OntologyRdfRepository.rep.writeAllStatements(outputFile)

    } else {

      val fmt = DateTimeFormat.forPattern("YYYY-MM-dd_hh-mm-ss")
      val textFileFolder = "./play-by-play-textfiles" / fmt.print(new LocalDateTime())
      textFileFolder.createIfNotExists(true, true)

      logger.info(s"Writing text files to folder ${textFileFolder.pathAsString}")

      val inputFolder = args(0).toFile
      if (!inputFolder.exists || !inputFolder.isDirectory)
        printUsageAndExit(Some(s"Invalid folder: ${inputFolder}"))

      if (inputFolder.isEmpty)
        printUsageAndExit(Some(s"No files found in folder: ${inputFolder}"))

      val playByPlayRegex = """^\d+\.html$"""

      val playByPlayFilenames = inputFolder.list.map(_.name).toArray
      val inputFiles = playByPlayFilenames
        .filter(!_.contains("-gameinfo"))
        .map(f => {
          val base = f.split("\\.").head
          (s"$base.html", s"$base-gameinfo.html")
        })
        .filter(e => playByPlayFilenames.contains(e._1) && playByPlayFilenames.contains(e._2))

      logger.info(s"Reading ${playByPlayFilenames.size} sets of play-by-play files from folder ${inputFolder}")

      var rep = new SailRepository(new MemoryStore)
      rep.initialize
      OntologyRdfRepository.addRdfPrefixes(rep)

      inputFiles
        .zipWithIndex
        .foreach(e => {
          val ((playByPlayFile, gameInfoFile), index) = e
          logger.info(s"Reading plays from $playByPlayFile and $gameInfoFile (file ${index + 1} of ${inputFiles.size})")
          try {
            val playByPlay: PlayByPlay = new EspnPlayByPlay(inputFolder.pathAsString, playByPlayFile, gameInfoFile)
            playByPlay.addRdf(rep)
            writeTextFile(playByPlay, textFileFolder)
          } catch {
            case e: InvalidPlayByPlayException => {
              logger.error(s"Error reading play-by-play: ${e.getMessage}")
            }
          }
        })

      logger.info("Finished reading files")

      rep.writeAllStatements(outputFile)

      logger.info(s"Finished writing Turtle")
      logger.info(s"Wrote text files to folder ${textFileFolder.pathAsString}")

    }
  }

  /**
   * Writes this play-by-play to a file in a folder, overwriting any existing file
   * @param folder   folder to write the file into
   */
  def writeTextFile(playByPlay: PlayByPlay, folder: better.files.File) = {

    if (playByPlay.textFileContents.isEmpty) {
      logger.warn(s"Did not write empty play-by-play file for ${playByPlay}")
    } else

      try {
        val filename = EntityUriFactory.getGameIdentifierString(playByPlay.homeTeam, playByPlay.awayTeam, playByPlay.gameTime) + ".txt"
        val file = folder / filename
        if (file.exists) {
          logger.info(s"Overwriting ${file.pathAsString}")
          file.delete(true)
        }
        file.writeText(playByPlay.textFileContents.get.mkString("\n"))
      } catch {
        case t: Throwable =>
          {
            logger.error(s"Error writing text file for game ${toString}")
          }
      }

  }
}
