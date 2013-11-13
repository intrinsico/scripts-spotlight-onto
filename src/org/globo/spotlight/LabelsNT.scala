package org.aksw.spotlight

import org.aksw.spotlight.util.FileUtils._
import java.util.Date
import java.io._
import scala.io.Source
import com.hp.hpl.jena.rdf.model.{StmtIterator, Model}

object LabelsNT {

  private val PREDICATE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label"
  private val OBJECT_LABEL = "http://xmlns.com/foaf/0.1/Person"
  private val INSERT_SPACE_UPPERCASE = "(\\p{Ll})(\\p{Lu})"  
    
  def generateLabelsNT(it: StmtIterator, output: String) {             
    
    var buffer = new StringBuilder        
    buffer.append("# started ".concat(new Date().toString()).concat("\n"))
    //appendToFile(output, buffer)

    var i = 0
    println("Creating labels file...")
    while (it.hasNext()) {

      if (i % 100000 == 0) println (i + " lines processed.")
      
      val stmt = it.nextStatement()
      val subject = stmt.getSubject()
      val predicate = stmt.getPredicate()
      val obj = stmt.getObject()
      val language = "@pt" //stmt.getLanguage()??

      if (predicate.toString().equals(PREDICATE_LABEL)) {

        val surfaceForm = obj.toString().replace("@pt", "")
        
        val tmpString = "<%s> <%s> \"%s\"%s .\n".format(subject, predicate, surfaceForm, language)
        buffer.append(tmpString)

        val label = subject.toString().split("/")
        val name = label(label.size - 1).replaceAll(INSERT_SPACE_UPPERCASE, "$1 $2")

        if (!tmpString.contains("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, name, language))) {
          buffer.append("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, name, language))
          //appendToFile(output, buffer)
        }

        if (!tmpString.contains("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, label(label.size - 1), language))) {
          buffer.append("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, label(label.size - 1), language))
          //appendToFile(output, buffer)
        }

      } else if (obj.toString().equalsIgnoreCase(OBJECT_LABEL)) {

        val label = subject.toString().split("/")
        val name = label(label.size - 1).replaceAll(INSERT_SPACE_UPPERCASE, "$1 $2")
        buffer.append("<%s> <%s> \"%s\"@en .\n".format(subject, PREDICATE_LABEL, name))        
      }
      
      if (i != 0 && i % 1000000 == 0 && !buffer.isEmpty) {
        appendToFile(output, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      } 
      
      i += 1
    }
    appendToFile(output, buffer.toString.dropRight(1))	
    //FileUtils.writeToFile(output, buffer.toString)

    println("Done.")
  }    
}