package org.globo.spotlight

import org.globo.spotlight.util.FileUtils._
import org.globo.spotlight.util.JenaUtils._
import java.util.Date
import java.io._
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.query.ResultSet

object InstanceTypesNT {    
  
  def generateInstanceTypesNT(placeTypeResult: ResultSet, personTypeResult: ResultSet, orgTypeResult: ResultSet, output: String) {
    val typesStream = new PrintStream(new File(output))            
    
    var i = 1
    println("Generating the instance types file...")
    if (placeTypeResult.hasNext || personTypeResult.hasNext || orgTypeResult.hasNext) {
      // Iterating the ResultSet to get all its elements
      while (placeTypeResult.hasNext) {
        val currentSolution = placeTypeResult.nextSolution()
        // <subject> <predicate> <object>        
        typesStream.println("<" + currentSolution.get("lugar") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + currentSolution.get("class") + "> .")
        typesStream.println("<" + currentSolution.get("lugar") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Place> .")
        
        if (i % 100000 == 0) println (i + " results processed.")
        i += 1
      }
      while (personTypeResult.hasNext) {
        val currentSolution = personTypeResult.nextSolution()
        // <subject> <predicate> <object>
        typesStream.println("<" + currentSolution.get("pessoa") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + currentSolution.get("class") + "> .")
        typesStream.println("<" + currentSolution.get("pessoa") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Person> .")

        if (i % 100000 == 0) println (i + " results processed.")
        i += 1
      }
      while (orgTypeResult.hasNext) {
        val currentSolution = orgTypeResult.nextSolution()
        // <subject> <predicate> <object>
        typesStream.println("<" + currentSolution.get("org") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + currentSolution.get("class") + "> .")
        typesStream.println("<" + currentSolution.get("org") + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Organisation> .")

        if (i % 100000 == 0) println (i + " results processed.")
        i += 1
      }

      println("Done.")
    } else {
      println("No entries for the types Person, Location e Organization.")
    }
  }
}