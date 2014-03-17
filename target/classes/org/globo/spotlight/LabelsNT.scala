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

import java.io._
import scala.io.Source
import com.hp.hpl.jena.query.ResultSet

object LabelsNT {
  
  def generateLabelsNT(labelsResults: ResultSet, output: String) {
    val labelsStream = new PrintStream(new File(output))
    
    var i = 1
    if (labelsResults.hasNext) {
      println("Creating Labels file, NT format...")
      // Iterating the ResultSet to get all its elements
      while (labelsResults.hasNext) {        
        val currentSolution = labelsResults.nextSolution()
        // Default object value is the label
        var label = currentSolution.get("o").toString
        var subject  = ""
          
        // Is it a person?          
        if (currentSolution.get("pessoa") != null) {
          // Is it an athlete?
          if (currentSolution.get("apel") != null) {
            label = currentSolution.get("apel").toString
          } else {
            // Has full name?
            if (currentSolution.get("nc") != null) {
              label = currentSolution.get("nc").toString
            }
          }
          subject = currentSolution.get("pessoa").toString
        // Is it a place?
        } else if (currentSolution.get("lugar") != null) {          
          subject = currentSolution.get("lugar").toString
        // Is it an organization?
        } else {
          if (currentSolution.get("nc") != null) {
            label = currentSolution.get("nc").toString
          }
          subject = currentSolution.get("org").toString
        }        
        
        // Final format according to the subject
        labelsStream.println("<" + subject + "> <http://www.w3.org/2000/01/rdf-schema#label> " + """"""" + label + """"@pt .""") 
        
        if (i % 100000 == 0) {
	      println(i + " results processed...")
	    }
        i += 1
      }      
      println("Done.")
    } else {
      println("No results for the labels file query.")
    }
  }
  
  def generateLabelsTSV(labelsResults: ResultSet, output: String) {
    val labelsStream = new PrintStream(new File(output))
    
    var i = 1
    if (labelsResults.hasNext) {
      println("Creating Labels file, TSV format...")
      // Iterating the ResultSet to get all its elements
      while (labelsResults.hasNext) {        
        val currentSolution = labelsResults.nextSolution()
        // Default object value is the label
        var label = currentSolution.get("o").toString
        var subject  = ""
          
        // Is it a person?          
        if (currentSolution.get("pessoa") != null) {
          // Is it an athlete?
          if (currentSolution.get("apel") != null) {
            label = currentSolution.get("apel").toString
          } else {
            // Has full name?
            if (currentSolution.get("nc") != null) {
              label = currentSolution.get("nc").toString
            }
          }
          subject = currentSolution.get("pessoa").toString
        // Is it a place?
        } else if (currentSolution.get("lugar") != null) {          
          subject = currentSolution.get("lugar").toString
        // Is it an organization?
        } else {
          if (currentSolution.get("nc") != null) {
            label = currentSolution.get("nc").toString
          }
          subject = currentSolution.get("org").toString
        }
        
        // Final format according to the subject
        labelsStream.println(label + "\t" + subject) 
        
        if (i % 100000 == 0) {
	      println(i + " results processed...")
	    }
        i += 1
      }      
      println("Done.")
    } else {
      println("No results for the labels file query.")
    }
  }
  
  def filterLabelsWithStopwords(labelsFile: String, stopwordsFile: String, outputFile: String) {
    val stopwordsSet = Source.fromFile(stopwordsFile).getLines().toSet
    
    println("Filtering a labels file in the NT format with a stopwords file...")
    val labelsStream = new PrintStream(new File(outputFile))
    for (line <- Source.fromFile(labelsFile).getLines()) {
      val lineArray = line.split(""""""")      
      if (lineArray.length == 3) {
        val label = lineArray(1)        
        if (!(stopwordsSet contains label.toLowerCase)) {
          labelsStream.println(line)
        }
      }
    }
    println("Done.")
  }
}