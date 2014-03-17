package org.globo.spotlight

/**
 * Created with IntelliJ IDEA.
 * User: Renan
 * Date: 17/03/14
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */

import scala.io.Source

object ExtractOccsFromGlobo {
  def saveOccsFile(dumpFile: String) {
    for (line <- Source.fromFile(dumpFile).getLines()) {
      //if (line.contains("title"))
    }
  }
}
