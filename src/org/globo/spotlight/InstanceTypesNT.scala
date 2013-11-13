package org.aksw.spotlight

import org.aksw.spotlight.util.FileUtils._
import org.aksw.spotlight.util.JenaUtils._
import java.util.Date
import java.io._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.ResultSet

object InstanceTypesNT {    
  
  def generateInstanceTypesNT(typesResults: ResultSet, output: String) {           
        
    var buffer = new StringBuilder    
    buffer.append("# started ".concat(new Date().toString()).concat("\n"))        
    
    var i = 0
    if (typesResults.hasNext) {
      println("Creating Instance Types file...")
      // Iterating the ResultSet to get all its elements
      while (typesResults.hasNext) {
        if (i % 100000 == 0) println (i + " results processed.")
        val currentSolution = typesResults.nextSolution() 
        // <subject> <predicate> <object>        
        buffer.append("<" + currentSolution.get("s") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + currentSolution.get("o") + "> .\n") 
        
        if (i != 0 && i % 1000000 == 0) {
	      appendToFile(output, buffer.toString.dropRight(1))
	      buffer.delete(0, buffer.length)
	      buffer = new StringBuilder
	    }
        i += 1
      }
      appendToFile(output, buffer.toString.dropRight(1))
      println("Done.")
    } else {
      println("No entries for the type person.")
    }
  }
}