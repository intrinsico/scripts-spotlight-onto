package org.aksw.spotlight

import com.hp.hpl.jena.rdf.model.{StmtIterator, Model}
import org.aksw.spotlight.util.FileUtils._
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.nio.charset.Charset
import java.io.IOException

object Redirects {
  private val PREDICATE_LABEL = "http://www.w3.org/2002/07/owl#sameAs"
  private val REDIRECTS_PREFIX = "http://semantica.globo.com/wikiPageRedirects"  
  
  def generateRedirectsFile(it: StmtIterator, outputFile: String) {
    var buffer = new StringBuilder            
    //appendToFile(output, buffer)

    var i = 0
    println("Creating redirects file...")
    while (it.hasNext()) {

      if (i % 10000 == 0) println (i + " lines processed.")
      
      val stmt = it.nextStatement()
      val subject = stmt.getSubject()
      val predicate = stmt.getPredicate()
      val obj = stmt.getObject()      

      if (predicate.toString().equals(PREDICATE_LABEL)) {
        buffer.append("<%s> <%s> <%s> .\n".format(subject, REDIRECTS_PREFIX, obj))
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
  
  def generateRedirects(it: StmtIterator, outputDir: String) {    
    generateRedirectsFile(it, outputDir + "redirects_globo.ttl")
  }
}