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
import java.net.URL
import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import java.nio.charset.Charset

object Wiki {
  
  val BEGIN_HTML = "DOCTYPE html"
  val END_HTML = "</html>"

  def generateWiki(turtleFile: String, outputFile: String) {
    
    var buffer = new StringBuilder
    var pageBuffer = new StringBuilder    

    buffer.append("""<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.6/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.6/ http://www.mediawiki.org/xml/export-0.6.xsd" version="0.6" xml:lang="pt">""" + "\n\t<siteinfo>\n\t\t<sitename>Globo</sitename>\n\t\t<base>http://semantica.globo.com</base>\n\t</siteinfo>\n")

    var i = 0
    for (line <- Source.fromFile(turtleFile).getLines()) {          
      
      try {
        if (!line.contains(END_HTML)) {	           
          pageBuffer.append(line)         
        } else {
          i += 1
          if (i % 100 == 0) println (i + " HTML pages processed.")                   
          val document = Jsoup.parse(pageBuffer.toString)         
	      buffer.append("\t<page>\n")
	      buffer.append("\t\t<title>")	
	      
	      // Get title according to case with UTF-8
	      var encBytes = document.title().replaceAll("&","e").getBytes()
          val title = new String(encBytes, "UTF-8") 	           
	      title match {
            case t if t.contains("|") => buffer.append(title.split("\\|")(0).dropRight(1))             
            case t if t.contains("no G1 AutoEsporte:") => buffer.append(title.split("no G1 AutoEsporte:")(0).dropRight(1))
            case t if t.contains("-") => buffer.append(title.split(" - ")(0))
            case _ => buffer.append(title)
          }	      
	      buffer.append("</title>\n")
	      buffer.append("\t\t<ns>0</ns>\n")
	      buffer.append("\t\t<id>" + i + "</id>\n")
	      buffer.append("\t\t<revision>\n")
	      buffer.append("\t\t\t<text>\n")
	      buffer.append("\t\t\t")
	      
	      encBytes = document.body().text().replaceAll("&","e").getBytes()
	      buffer.append(new String(encBytes, "UTF-8"))
	      
	      buffer.append("\n")
	      buffer.append("\t\t\t</text>\n")
	      buffer.append("\t\t</revision>\n")
	      buffer.append("\t</page>\n")
	      
	      pageBuffer.delete(0, buffer.length)
          pageBuffer = new StringBuilder
          
          if (i % 100 == 0 && !buffer.isEmpty) {
	        appendToFile(outputFile, buffer.toString.dropRight(1))
	        buffer.delete(0, buffer.length)
	        buffer = new StringBuilder
          }                   
	    }    
      } catch {
        case e: IOException => println("An error occurred while parsing this page!")
      }
    }

    buffer.append("</mediawiki>")
    
    appendToFile(outputFile, buffer.toString)    
  }
}