package org.aksw.spotlight

import scala.io.Source
import com.hp.hpl.jena.rdf.model.{StmtIterator, Model}
import java.util.Date
import org.aksw.spotlight.util.FileUtils._
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.nio.charset.Charset
import java.io.IOException

object Context {
  
  private val PREDICATE_LABEL = "http://semantica.globo.com/base/url_do_permalink"
  
  def generatePermalinksFile(it: StmtIterator, outputFile: String) {
    var buffer = new StringBuilder            
    //appendToFile(output, buffer)

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
    //buffer.append("# started ".concat(new Date().toString()).concat("\n"))
    //appendToFile(output, buffer)

    var i = 0
    println("Creating the context file...")
    
    for (line <- Source.fromFile(inputFile).getLines()) {
      
      if (i % 1 == 0) println (i + " HTML pages processed.")
      
      val lineArray = line.split(' ')
      
      //val fdp = Source.fromURL(lineArray(2).dropRight(1).reverse.dropRight(1).reverse).mkString
      //println (fdp)
      //System.exit(1)
      
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
    appendToFile(outputFile, buffer.toString.dropRight(1))
  }
  
  def generateContext(it: StmtIterator, outputDir: String) {
    //generatePermalinksFile(it, outputDir + "context_globo.ttl")
    generateContextFile(outputDir + "mini_mini_permalinks_globo.ttl", outputDir + "context_globo.ttl")
    //generateContextFile(outputDir + "permalinks_small.ttl", outputDir + "context_globo.ttl")        
    
	//val html = Source.fromURL("http://google.com")
	//val s = html.mkString
  }
}