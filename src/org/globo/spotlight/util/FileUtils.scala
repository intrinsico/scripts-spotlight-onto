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

package org.globo.spotlight.util

import java.io._
import java.text.Normalizer
import java.util.regex.Pattern
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.nio.charset.Charset
import scala.io.Source
import org.apache.commons.lang.StringEscapeUtils

object FileUtils {  
  
  def convertFormat(aFile: String, output: String) {
    
    implicit val codec = Codec("iso-8859-1")    
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)
    var buffer = new StringBuilder
    var i = 1
    
    for (line <- Source.fromFile(aFile).getLines()) {
      try {
        buffer.append(new String(line.getBytes("iso-8859-1")))
        buffer.append("\n")
        
        if (i % 100000 == 0 && !buffer.isEmpty) {
          println ("Globo file current line = " + i)
          appendToFile(output, buffer.toString.dropRight(1))
    	  buffer.delete(0, buffer.length)
    	  buffer = new StringBuilder
        }
      } catch {
        case e: IOException => println("An error occurred while parsing this string!")
      }
      
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }
  }
  
  def fixOccs(ori_occs: String, final_occs: String) {
    
    implicit val codec = Codec("UTF-8")    
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    for (line <- Source.fromFile(ori_occs).getLines()) {
      val lineArray = line.split('\t')
      appendToFile(final_occs, lineArray(1) + '\t' + lineArray(0) + '\t' + lineArray(2) + '\t' + lineArray(3) + '\t' + lineArray(4))
    }
  }
  
  def createDir(dirPath: String) {
	val theDir = new File(dirPath)
	
	// if the directory does not exist, create it
	if (!theDir.exists()) {
	  println("Creating directory: " + dirPath)
	  val result = theDir.mkdir()
	
	  if(result) {    
	    println("Done.")  
	  } else {
	    println("An error occurred!")
	    System.exit(1)
	  }
	} else {
	  println("Folder already exists, skipping creation!")
	}
  }
  
  def writeToFile(file: String, content: String) {
    val pw = new java.io.PrintWriter(new java.io.File(file))
    try {
      pw.write(content)
    } finally {
      pw.close()
    }
  }
  
  // Utility function to append the final string to the initial instance types triples file
  def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B = {
    try { f(param) } finally { param.close() }
  }

  def appendToFile(fileName:String, textData:String) = {
    using (new FileWriter(fileName, true)){
      fileWriter => using (new PrintWriter(fileWriter)) {
        printWriter => printWriter.println(textData)
      }
    }
  }
  
  def generateDataset(dirPath: String, outputFile: String) {
    implicit val codec = Codec("iso-8859-1")    
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)
	//codec.onMalformedInput(CodingErrorAction.REPLACE)
    //codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    println("Generating a single dataset file.")    
    var i = 1    
    for (file <- new java.io.File(dirPath).listFiles.map(_.getName).toList) {      
      println ("File number " + i + " with name " + file.toString + " being processed...")      
      preprocessTurtle(dirPath + file, outputFile)
      println("Done.")
      i += 1
    }
  }
  
  def preprocessTurtle(turtleFile: String, outputFile: String) {
      
    implicit val codec = Codec("UTF-8")    
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    var buffer = new StringBuilder      
    var i = 0
    
    for(scan <- Source.fromFile(turtleFile).getLines()) {
      if (i % 100000 == 0) println (i + " lines processed.")
      if (i != 0 && i % 1000000 == 0) {
        FileUtils.appendToFile(outputFile, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      }      
      val currentString = StringEscapeUtils.unescapeJava(scan).replaceAll("\\\\", "")                                   
            
      val testArray = currentString.split("\t")      
      if (!scan.contains("A banda de pop teen Restart") && !scan.contains("de death metal Cannibal Corpse")) {
        if (currentString.count(_ == '\t') == 2) {
          if (testArray(2).split('\"').length <= 3 && currentString.endsWith(" .") && !testArray(2).endsWith(";") && !testArray(2).endsWith(",") && !testArray(1).contains("owl:priorVersion") && (testArray(0) != "" && testArray(1) != "")) {            
            val auxString0 = new String(testArray(0).getBytes("UTF-8"))
            val auxString2 = new String(testArray(2).getBytes("UTF-8"))
            if (auxString0.contains("ï¿½") || (auxString2.contains("ï¿½") && auxString2.count(_ == '\"') == 0)) {
              testArray(0) = auxString0.replaceAll("_ï¿½", "_Í").replaceAll("ï¿½", "í")
              testArray(2) = auxString2.replaceAll("_ï¿½", "_Í").replaceAll("ï¿½", "í")
              buffer.append(testArray(0) + '\t' + testArray(1) + '\t' + testArray(2) + '\n')
            } else {            
              buffer.append(currentString.replaceAll("\n", "") + '\n')            
            }
          }
        } else {
    	  buffer.append(currentString + '\n')
        }
      }
            
      i += 1
    } 
    FileUtils.appendToFile(outputFile, buffer.toString.dropRight(1))
  }
}


