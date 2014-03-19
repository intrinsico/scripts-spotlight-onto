package org.globo.spotlight

/**
 * Created with IntelliJ IDEA.
 * User: Renan
 * Date: 17/03/14
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */

import scala.io.Source
import scala.collection.mutable
import java.io.PrintStream

object ExtractOccsFromGlobo {
  val IDLE_STATE = 0
  val TITLE_STATE = 1
  val TEXT_STATE = 2
  val END_STATE = 3
  var currentState = IDLE_STATE
  var oldState = IDLE_STATE

  var currentTitle = ""
  var currentContext = ""
  var currentParagraph = ""
  var currentOffset = -1

  def updateCurrentState(currentLine: String): Int = {
    currentLine match {
      case x if x.contains("<title>") => TITLE_STATE
      case x if x.contains("<ns>") => IDLE_STATE
      case x if x.contains("<text>") => TEXT_STATE
      case x if x.contains("</text>") => END_STATE
      case _ => {
        if (!oldState.equals(TEXT_STATE)) IDLE_STATE
        else TEXT_STATE
      }
    }
  }

  def saveOccsFile(dumpFile: String, titlesFile: String, output: String) {
    val titlesHash = new mutable.HashMap[String, (String, String)]()
    val occsStream = new PrintStream(output)

    for (line <- Source.fromFile(titlesFile).getLines()) {
      val lineArray = line.split('\t')
      if (lineArray.length == 3) {
        titlesHash += lineArray(2) -> (lineArray(0), lineArray(1))
      }
    }

    for (line <- Source.fromFile(dumpFile).getLines()) {
      oldState = currentState
      currentState = updateCurrentState(line)

      currentState match {
        case x if x.equals(TITLE_STATE) => {
          currentTitle = line.trim.replaceFirst("<title>","").replaceFirst("</title>","")
        }
        case x if x.equals(TEXT_STATE) => {
          if (!line.contains("<text>")) {
            currentContext = line.replaceAll("\t","")
            currentOffset = currentContext.indexOfSlice(currentTitle)
          }
        }
        case x if x.equals(END_STATE) => {
          val currentValue = titlesHash.getOrElse(currentTitle, ("", ""))
          if (currentValue._1 != "") {
            occsStream.println(currentTitle+"-p1l1" + '\t' + currentValue._1 + '\t' + currentTitle + '\t' + currentContext + '\t' + currentOffset.toString)
          }
          currentTitle = ""
          currentContext = ""
          currentOffset = -1
          currentState = IDLE_STATE
        }
        case _ =>
      }
    }
  }

  def saveTSVFromOccs(occsFile: String, output: String) {
    val tsvStream = new PrintStream(output)

    println("Saving the TSV file from the Globo occs...")
    for (line <- Source.fromFile(occsFile).getLines()) {
      val lineArray = line.split('\t')
      if (lineArray.length >= 3) {
        tsvStream.println(lineArray(2) + '\t' + lineArray(1))
      }
    }
    println("Done.")
  }
}
