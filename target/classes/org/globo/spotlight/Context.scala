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

import scala.io.Source
import com.hp.hpl.jena.rdf.model.StmtIterator
import org.globo.spotlight.util.FileUtils._
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.io._
import org.jsoup.Jsoup

object Context {
  
  private val PREDICATE_LABEL = "http://semantica.globo.com/base/url_do_permalink"
  
  def generatePermalinksFile(it: StmtIterator, outputFile: String) {
    var buffer = new StringBuilder                

    var i = 0
    println("Creating permalinks file...")
    while (it.hasNext()) {

      if (i % 10000 == 0) println (i + " lines processed.")
      
      val stmt = it.nextStatement()
      val subject = stmt.getSubject()
      val predicate = stmt.getPredicate()
      val obj = stmt.getObject()      

      if (predicate.toString().equals(PREDICATE_LABEL)) {
        buffer.append("<%s> <%s> <%s> .\n".format(subject, predicate, obj))
      }
      
      if (i != 0 && i % 1000000 == 0 && !buffer.isEmpty) {
        appendToFile(outputFile, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      } 
      
      i += 1
    }
    appendToFile(outputFile, buffer.toString.dropRight(1))
  }
  
  def generateContextFile(inputFile: String, outputFile: String) {
    implicit val codec = Codec("UTF-8")    
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    var buffer = new StringBuilder            
    var i = 0
    println("Creating the context file...")
    
    for (line <- Source.fromFile(inputFile).getLines()) {
      
      if (i % 1 == 0) println (i + " HTML pages processed.")
      
      val lineArray = line.split(' ')
      
      // Extra space before the dot!
      if (lineArray.length == 4) {        
        val URLpath = lineArray(2).dropRight(1).reverse.dropRight(1).reverse        
        try {
          buffer.append(Source.fromURL(URLpath).mkString + '\n')
        } catch {
          case e: IOException =>
        }        
      }
      
      if (i != 0 && i % 50 == 0) {
        appendToFile(outputFile, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      } 
      
      i += 1
    }
    appendToFile(outputFile, buffer.toString().dropRight(1))
  }

  def generateContextTitles(permalinksFile: String, output: String) {
    val titlesStream = new PrintStream(output)
    var i = 1

    println("Generating the Globo titles file...")
    for (line <- Source.fromFile(permalinksFile).getLines()) {
      val lineArray = line.split(' ')

      // Extra space before the dot!
      if (lineArray.length == 4) {
        val resource = lineArray(0).dropRight(1).reverse.dropRight(1).reverse
        val URLpath = lineArray(2).dropRight(1).reverse.dropRight(1).reverse

        try {
          val document = Jsoup.connect(URLpath).get()
          titlesStream.println(resource + '\t' + URLpath + '\t' + Wiki.getCorrectTitle(document.title()))
        } catch {
          case e: IOException => println("Error processing this page: " + URLpath)
        }
      }
    }
    println("Done.")
  }
  
  def generateContext(it: StmtIterator, outputDir: String) { 
    generatePermalinksFile(it, outputDir + "permalinks.ttl")
    generateContextFile(outputDir + "permalinks.ttl", outputDir + "context_globo.ttl")            
  }
}