package org.globo.spotlight

import org.globo.spotlight.util.FileUtils._
import org.globo.spotlight.util.JenaUtils._
import java.util.Date
import java.io._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.ResultSet

object InstanceTypesNT {    
  
  def generateInstanceTypesNT(typesResults: ResultSet, output: String) {           
    val typesStream = new PrintStream(new File(output))            
    
    var i = 0
    println("Generating the instance types file...")
    if (typesResults.hasNext) {
      println("Creating Instance Types file...")
      // Iterating the ResultSet to get all its elements
      while (typesResults.hasNext) {        
        val currentSolution = typesResults.nextSolution() 
        // <subject> <predicate> <object>        
        typesStream.println("<" + currentSolution.get("s") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + currentSolution.get("o") + "> .") 
        
        if (i % 100000 == 0) println (i + " results processed.")
        i += 1
      }      
      println("Done.")
    } else {
      println("No entries for the type person.")
    }
  }
}