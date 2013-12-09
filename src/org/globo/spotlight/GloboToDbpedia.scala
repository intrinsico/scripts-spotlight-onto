package org.globo.spotlight

import scala.io.Source
import scala.math.Ordering.Char._
import scala.collection.mutable.ListBuffer
import scala.util.control._
import org.globo.spotlight.util.FileUtils._

object GloboToDbpedia {
  def filterLabels(glbLabelsFile: String, allowedTypesFile: String, output: String) {
    var buffer = new StringBuilder
    var i = 1
    var j = 1
    
    val typesList = new ListBuffer[String]
    
    for (line <- Source.fromFile(allowedTypesFile).getLines()) {      
      typesList += line
    }
    
    val loop = new Breaks
    for (line <- Source.fromFile(glbLabelsFile).getLines().drop(1)) {
      val firstColumn = line.split(' ')(0) 
      loop.breakable {
        for (aType <- typesList) {
          if (firstColumn.contains(aType)) {
            buffer.append(line + "\n")
            
            if (i % 100000 == 0 && !buffer.isEmpty) {
        	  appendToFile(output, buffer.toString.dropRight(1))
        	  buffer.delete(0, buffer.length)
        	  buffer = new StringBuilder
        	  
        	  println(i + " lines processed.")
            }
            
            i += 1
            loop.break
          }
        }
      }
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }

    println("Done.")
  }
  
  def levenshtein(str1: String, str2: String): Int = {
    val lenStr1 = str1.length
    val lenStr2 = str2.length
 
    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)
 
    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j
 
    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j-1)) 0 else 1
 
      d(i)(j) = min(
        d(i-1)(j  ) + 1,     // deletion
        d(i  )(j-1) + 1,     // insertion
        d(i-1)(j-1) + cost   // substitution
      )
    }
 
    d(lenStr1)(lenStr2)
  }
 
  def min(nums: Int*): Int = nums.min  
  
  def generateGlbDbMapping(glbLabelsFile: String, dbpediaLabelsFile: String, output: String) {    
    var buffer = new StringBuilder
    var i = 1
    var j = 1
        
    val dbFirstColumn = new ListBuffer[String]
    val dbSecondColumn = new ListBuffer[String]
    val dbThirdColumn = new ListBuffer[(String, Int)]
    
    var z = 0
    for (line <- Source.fromFile(dbpediaLabelsFile).getLines().drop(1)) {      
      val dbLineArray = line.split(" ",3)
	  // Filter only entries starting with letters
      if (dbLineArray.length >= 3 && (dbLineArray(2)(1).toLower.toInt >= 97 && dbLineArray(2)(1).toLower.toInt <= 122)) {
        dbFirstColumn += dbLineArray(0)
        dbSecondColumn += dbLineArray(1)
        dbThirdColumn += Tuple2(dbLineArray(2), z)
        z += 1
      }      
    }
    
    for (glbLine <- Source.fromFile(glbLabelsFile).getLines().drop(1)) {
      val glbLineArray = glbLine.split(" ",3)
      
      if (glbLineArray.length >= 3) {          
        dbThirdColumn.find((x: Tuple2[String, Int]) => x._1 == glbLineArray(2)) match {
          case Some(x) => {            
            buffer.append(glbLineArray(0) + " <http://www.w3.org/2002/07/owl#sameAs> " + dbFirstColumn(x._2) + " .\n")
	          
            if (j % 100 == 0 && !buffer.isEmpty) {
              println ("Globo file current line = " + i)
              appendToFile(output, buffer.toString.dropRight(1))
    	      buffer.delete(0, buffer.length)
    	      buffer = new StringBuilder
            }
      
            j += 1
          }
          case None =>   
        }
                 
        if (i % 10000 == 0) println (i + " lines processed.")
        i += 1
      }
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }

    println("Done.")
  }
}