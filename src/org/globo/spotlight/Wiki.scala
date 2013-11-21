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

  // Gets the that prevents encoding error according to the length of the string
  def getCorrectString(utfString: String, isoString: String): String = {
    if (isoString.length <= utfString.length) {
      isoString
    } else {
      utfString
    } 
  }
  
  // Strips the title of unnecessary information according to the each origin
  def getCorrectTitle(aTitle: String): String = {
    aTitle match {
      case t if t.contains("|") => aTitle.split("\\|")(0).dropRight(1)             
      case t if t.contains("no G1") => aTitle.split("no G1")(0).dropRight(1)
      case t if (t.startsWith("EGO ") && t.split(" - ").length >= 2) => aTitle.split(" - ")(1)
      case t if t.contains("-") => aTitle.split(" - ")(0)
      case _ => aTitle
    }
  }
    
  def generateWiki(turtleFile: String, outputFile: String) {
    
    val BEGIN_HTML = "DOCTYPE html"
    val END_HTML = "</html>"
    val DEFAULT_ENCODING = "iso-8859-1"
    var buffer = new StringBuilder
    var pageBuffer = new StringBuilder    

    buffer.append("""<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.6/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.6/ http://www.mediawiki.org/xml/export-0.6.xsd" version="0.6" xml:lang="pt">""" + "\n\t<siteinfo>\n\t\t<sitename>Globo</sitename>\n\t\t<base>http://semantica.globo.com</base>\n\t</siteinfo>\n")    
    
    var i = 1
    for (line <- Source.fromFile(turtleFile).getLines()) {          
      
      try {
        if (!line.contains(END_HTML)) {	           
          pageBuffer.append(line)         
        } else {          
          if (i % 200 == 0) println (i + " HTML pages processed.")            
          val document = Jsoup.parse(pageBuffer.toString)  
          pageBuffer.delete(0, pageBuffer.length)
	      pageBuffer = new StringBuilder
	      
	      // Get title according to case with UTF-8	      
	      var encBytes = document.title().replaceAll("&","e").getBytes(DEFAULT_ENCODING)	      	                     
	      val title = getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING)))	                          	  	      	      	      	                   
	      	      
          // Skip pages without og:description property because this property defines the context
	      encBytes = null
	      encBytes = document.select("meta[property=og:description]").attr("content").replaceAll("&","e").getBytes(DEFAULT_ENCODING)
	      if (encBytes.length != 0) {	      		   
	        var context = getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING))		    		      	            	     	      	                   
		      
		    // The final part is to append the actual page to the buffer
		    buffer.append("\t<page>\n")
		    buffer.append("\t\t<title>")
		    buffer.append(title)
		    buffer.append("</title>\n")
		    buffer.append("\t\t<ns>0</ns>\n")
		    buffer.append("\t\t<id>" + i + "</id>\n")
		    buffer.append("\t\t<revision>\n")
		    buffer.append("\t\t\t<text>\n")
		    buffer.append("\t\t\t")
		    buffer.append(context)	      	    		     	      
		    buffer.append("\n")
		    buffer.append("\t\t\t</text>\n")
		    buffer.append("\t\t</revision>\n")
		    buffer.append("\t</page>\n")		      		  
	          
	        if (i % 200 == 0 && !buffer.isEmpty) {
		      appendToFile(outputFile, buffer.toString.dropRight(1))
		      buffer.delete(0, buffer.length)
		      buffer = new StringBuilder
	        }   
	        i += 1	    
	      }          
          encBytes = null
	    }    
      } catch {
        case e: IOException => println("An error occurred while parsing this page!")
      }
    }

    buffer.append("</mediawiki>")
    
    appendToFile(outputFile, buffer.toString)    
  }
}