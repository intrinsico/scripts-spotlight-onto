package org.globo.spotlight

/**
 * Created with IntelliJ IDEA.
 * User: Renan
 * Date: 17/03/14
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */

import scala.io.Source
import scala.collection.parallel.mutable
import java.io.PrintStream

object ExtractOccsFromGlobo {
  val IDLE_STATE = 0
  val TITLE_STATE = 1
  val TEXT_STATE = 2
  val END_STATE = 3
  var currentState = IDLE_STATE

  var currentTitle = ""
  var currentContext = ""
  var currentParagraph = ""
  var currentOffset = "-1"

  def updateCurrentState(currentLine: String): Int = {
    currentLine match {
      case x if x.contains("<title>") => TITLE_STATE
      case x if x.contains("<ns>") => IDLE_STATE
      case x if x.contains("<text>") => TEXT_STATE
      case x if x.contains("</text>") => END_STATE
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
      currentState = updateCurrentState(line)

      currentState match {
        case x if x == TITLE_STATE => {
          currentTitle = line.replaceFirst("<title>","").replaceFirst("</title>","")
        }
        case x if x == TEXT_STATE => {
          if (!line.contains("<text>")) currentContext = line
        }
        case x if x == END_STATE => {
          val currentValue = titlesHash.getOrElse(currentTitle, ("", ""))
          if (currentValue._1 != "") {
            occsStream.println(currentTitle+"-p1l1" + '\t' + currentValue._2 + '\t' + currentTitle + '\t' + currentContext + '\t' + "-1")
          }
          currentTitle = ""
          currentContext = ""
        }
      }

    }
  }
}
