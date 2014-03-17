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

import com.hp.hpl.jena.rdf.model.{ModelFactory, Model}
import com.hp.hpl.jena.util.FileManager
import org.globo.spotlight.util.SparqlUtils
import net.liftweb.json._
import org.globo.spotlight.util.FileUtils._
import org.globo.spotlight.util.JenaUtils._
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
    
  // Strips the context of unnecessary information according to the each origin
  def getCorrectContext(document: org.jsoup.nodes.Document): String = {
    val DEFAULT_ENCODING = "iso-8859-1"
    
    var encBytes = document.title().replaceAll("&","e").getBytes(DEFAULT_ENCODING)	      	                     
	val title = getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING))
	var returnValue = ""    	
	  
    title match {
      case t if (t.contains("G1") && t.contains("|") && (t.split("\\|").length == 2)) => {               
        returnValue = ""        
      }      
      // Testar esse case! tava com acento
      case t if (t.contains("G1") && t.contains("|") && t.contains("Pol\\U+00EDtico")) => {                        
        if(!document.select("div.widget-ficha-candidato").isEmpty()) returnValue = document.select("div.widget-ficha-candidato").first().text() else returnValue = ""        
      }
      case t if (t.contains("G1") && t.contains("Educa")) => {                        
        if(!document.select("div.informacoes").isEmpty()) returnValue = document.select("div.informacoes").first().text() else returnValue = ""
        if(!document.select("div.infos").isEmpty()) returnValue = returnValue + document.select("div.infos").first().text()           
      }
      case t if (t.contains("G1")) => {                        
        if(!document.select("meta[property=og:description]").isEmpty()) returnValue = document.select("meta[property=og:description]").attr("content").replaceAll("&","e") else returnValue = ""        
      }      
      case t if t.contains("globoesporte.com") => {               
        if(!document.select("div.atleta-box-esporte").isEmpty()) returnValue = document.select("div.atleta-box-esporte").first().text() else returnValue = ""        
      }
      case t if t.contains("Combate") => {        
        if(!document.select("div.organizacao-lutador").isEmpty()) returnValue = document.select("div.organizacao-lutador").first().text() else returnValue = ""        
      }      
      case t if (t.startsWith("EGO ")) => {               
        if(!document.select("div.painel-biografia").isEmpty()) returnValue = document.select("div.painel-biografia").first().text().replace("Biografiaexibir voltar ","") else returnValue = ""        
      }
      case _ => returnValue = ""
    }
            
    returnValue    
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////////
  
  def complementContext(context: String, relationString: String, globoModel: Model): String = {
    val DEFAULT_ENCODING = "iso-8859-1"
    
    val PREDICATE_SEARCHABLE = "http://semantica.globo.com/base/dados_buscaveis"
    val PREDICATE_DESCRIPTION = "http://semantica.globo.com/base/descricao"
    val PREDICATE_G1_OCUPATION = "http://semantica.globo.com/G1/ocupacao"
    
    val relationQuery = queryUri("<"+relationString+">")
    val relationResults = executeQuery(relationQuery, globoModel) 
    
    var returnString = context
    
    if (relationResults.hasNext) {        
      // Iterating the ResultSet to get all its elements
      while (relationResults.hasNext) {                    
        val currentSolution = relationResults.nextSolution()
        //println (currentSolution.get("s") + " " + currentSolution.get("p") + " " + currentSolution.get("o"))
        if (currentSolution.get("p").toString == PREDICATE_SEARCHABLE && (!context.contains(currentSolution.get("o").toString))) {
          val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
          returnString = returnString + (" " + getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING))))
        } else if (currentSolution.get("p").toString == PREDICATE_DESCRIPTION) {
          val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
          returnString = returnString + (" " + getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING))))          
        } else if (currentSolution.get("p").toString == PREDICATE_G1_OCUPATION) {
          val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
          returnString = returnString + (" " + getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING))))
        }
      }        
    }
    returnString
  }
  
  def generateWikiJena(linksFile: String, outputFile: String, globoModel: Model) {
    val BEGIN_HTML = "DOCTYPE html"
    val END_HTML = "</html>"
    val DEFAULT_ENCODING = "UTF-8"
      
    val PREDICATE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label"
    val PREDICATE_FULL_NAME = "http://semantica.globo.com/base/nome_completo"
    val PREDICATE_RELATION = "http://semantica.globo.com/base/desempenhado_por"
    val PREDICATE_DESCRIPTION = "http://semantica.globo.com/base/descricao"
      
    var buffer = new StringBuilder
    var pageBuffer = new StringBuilder    

    buffer.append("""<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.6/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.6/ http://www.mediawiki.org/xml/export-0.6.xsd" version="0.6" xml:lang="pt">""" + "\n\t<siteinfo>\n\t\t<sitename>Globo</sitename>\n\t\t<base>http://semantica.globo.com</base>\n\t</siteinfo>\n")
    
    var i = 1
    for (line <- Source.fromFile(linksFile).getLines()) {
      val uriArray = line.split(' ')(0).split('/')
      val uri = uriArray(uriArray.length-1).dropRight(1)
          
      var title = ""
      var context = ""
      var relationString = ""
      var hasDescription = false      
      
      try {        
        val testsQuery = queryUri(line.split(' ')(0))        
        val testResults = executeQuery(testsQuery, globoModel)
        if (testResults.hasNext) {           
          // Iterating the ResultSet to get all its elements
          while (testResults.hasNext) {                    
            val currentSolution = testResults.nextSolution()
            //println (currentSolution.get("p") + " " + currentSolution.get("o"))
            if (currentSolution.get("p").toString == PREDICATE_LABEL) {              
              val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING) 
              title = getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING)))
            } else if (currentSolution.get("p").toString == PREDICATE_FULL_NAME) {
              val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
              context = getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING)))
            } else if (currentSolution.get("p").toString == PREDICATE_DESCRIPTION) {
              val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
              context = getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING)))
              hasDescription = true
            } else if (currentSolution.get("p").toString == PREDICATE_RELATION && hasDescription == false) {              
              val encBytes = currentSolution.get("o").toString.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
              relationString = getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING)))
            }
          }            
        }
      
        if (title != "" && context != "") {
          // Complement the context if it has complement
          if (relationString != "") {
            context = complementContext(context, relationString, globoModel)
          }
          if (i % 200 == 0) println (i + " HTML pages processed.")
        
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
      } catch {
        case e: IOException => println("An error occurred while parsing this page!")
        case e: com.hp.hpl.jena.query.QueryParseException => println("Query parse error occurred while parsing this page!")
      }
    }
    
    buffer.append("</mediawiki>")
    
    appendToFile(outputFile, buffer.toString)
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////////  
  def generateWikiHTML(turtleFile: String, outputFile: String) {               
    
    val BEGIN_HTML = "DOCTYPE html"
    val END_HTML = "</html>"
    val DEFAULT_ENCODING = "iso-8859-1"
    var buffer = new StringBuilder
    var pageBuffer = new StringBuilder    

    buffer.append("""<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.6/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.6/ http://www.mediawiki.org/xml/export-0.6.xsd" version="0.6" xml:lang="pt">""" + "\n\t<siteinfo>\n\t\t<sitename>Globo</sitename>\n\t\t<base>http://semantica.globo.com</base>\n\t</siteinfo>\n")    
    
    var i = 1
    var currentPages = 0
    for (line <- Source.fromFile(turtleFile).getLines()) {          
      
      try {
        if (!line.contains(END_HTML)) {	           
          pageBuffer.append(line)         
        } else {          
          if (i % 200 == 0 && i != currentPages) {
            currentPages = i
            println (currentPages + " HTML pages processed.")            
          }             
          val document = Jsoup.parse(pageBuffer.toString)  
          pageBuffer.delete(0, pageBuffer.length)
	      pageBuffer = new StringBuilder
	      
	      // Get title according to case with UTF-8	  	      
	      var encBytes = document.title().replaceAll("&","e").getBytes(DEFAULT_ENCODING)	      	                     
	      val title = getCorrectTitle(getCorrectString(new String(encBytes, "UTF-8"), new String(encBytes, DEFAULT_ENCODING)))	                          	  	      	      	      	                   
	      	      
          // Skip pages without og:description property because this property defines the context	      
	      encBytes = null
	      val correctContext = getCorrectContext(document)
	      //encBytes = document.select("meta[property=og:description]").attr("content").replaceAll("&","e").getBytes(DEFAULT_ENCODING)
	      encBytes = correctContext.replaceAll("&","e").getBytes(DEFAULT_ENCODING)
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