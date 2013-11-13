/* Copyright 2012 Intrinsic Ltda.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Check our project website for information on how to acknowledge the
* authors and how to contribute to the project:
* http://spotlight.dbpedia.org
*
*/

package org.globo.spotlight

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.util.FileManager
import org.globo.spotlight.util.SparqlUtils
import net.liftweb.json._
import org.globo.spotlight.util.FileUtils._
import java.util.Date
import scala.io.Source
import java.io.IOException
import org.jsoup.Jsoup

object Wiki {
  
  val BEGIN_HTML = "DOCTYPE html"
  val END_HTML = "</html>"

  def generateWiki(turtleFile: String, outputFile: String) {
    
    var buffer = new StringBuilder
    var pageBuffer = new StringBuilder    

    buffer.append("<mediawiki>\n")

    var i = 0
    for (line <- Source.fromFile(turtleFile).getLines()) {          
      
      try {
        if (!line.contains(END_HTML)) {	           
          pageBuffer.append(line)         
        } else {
          if (i % 100 == 0) println (i + " HTML pages processed.")
          
          val document = Jsoup.parse(pageBuffer.toString)
	      buffer.append("\t<page>\n")
	      buffer.append("\t\t<title>")	    	        
	      buffer.append(document.title())
	      buffer.append("</title>\n")
	      buffer.append("<ns>0</ns>")
	      buffer.append("<id>1</id>")
	      buffer.append("\t\t<revision>\n")
	      buffer.append("\t\t\t<text>\n")
	      buffer.append("\t\t\t")
	      buffer.append(document.body().text())
	      buffer.append("\n")
	      buffer.append("\t\t\t</text>\n")
	      buffer.append("\t\t</revision>\n")
	      buffer.append("\t</page>\n")
	      
	      pageBuffer.delete(0, buffer.length)
          pageBuffer = new StringBuilder
          
          if (i != 0 && i % 100 == 0 && !buffer.isEmpty) {
	        appendToFile(outputFile, buffer.toString.dropRight(1))
	        buffer.delete(0, buffer.length)
	        buffer = new StringBuilder
          }
          
          i += 1
	    }    
      } catch {
        case e: IOException =>
      }
    }

    buffer.append("</mediawiki>")
    
    appendToFile(outputFile, buffer.toString.dropRight(1))    
  }
}