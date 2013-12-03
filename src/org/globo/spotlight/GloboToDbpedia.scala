package org.globo.spotlight

import scala.io.Source
import org.globo.spotlight.util.FileUtils._

object GloboToDbpedia {
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
    
    for (line <- Source.fromFile(glbLabelsFile).getLines().drop(1)) {
      val glbLineArray = line.split(' ')
      val glbLabel = glbLineArray(2).replaceAll("""["@pt]""","")              
      
      for (line <- Source.fromFile(dbpediaLabelsFile).getLines().drop(1)) {        
        val dbLineArray = line.split(' ')
        val dbLabel = dbLineArray(2).replaceAll("""["@pt]""","")                       
        
        if (levenshtein(glbLabel, dbLabel) == 1) {
          //println("DB = " + dbLabel)
          //println("GLB = " + glbLabel)          
          buffer.append(glbLineArray(0) + " <http://www.w3.org/2002/07/owl#sameAs> " + dbLineArray(0) + " .\n")
          
          if (j % 10000 == 0 && !buffer.isEmpty) {
        	appendToFile(output, buffer.toString.dropRight(1))
        	buffer.delete(0, buffer.length)
        	buffer = new StringBuilder
          }
          
          j += 1
        }
      }
      
      if (i % 10000 == 0) println (i + " lines processed.")
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }

    println("Done.")
  }
}