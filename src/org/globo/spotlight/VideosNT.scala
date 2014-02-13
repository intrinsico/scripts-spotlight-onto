package org.globo.spotlight

import scala.io.Source
import org.globo.spotlight.util.FileUtils._
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.nio.charset.Charset
import java.lang.Character._
import scala.util.control._
import scala.collection.mutable.ListBuffer

object VideosNT {
  
  val onlyDigitsRegex = "^\\d*$".r
  
  def isAllDigits(x: String) = x match {
    case onlyDigitsRegex() => true
    case _ => false
  }
  
  def generateWordList(videosFile: String, output: String) {
    
    implicit val codec = Codec("iso-8859-1")    
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)
    
    var buffer = new StringBuilder
    
    val wordsListBuffer = new ListBuffer[String]
    var i = 1
    var j = 1
    
    for (line <- Source.fromFile(videosFile).getLines()) {
      val lineArray = line.split('\t')      
      if (lineArray(1) == "rdfs:label" && lineArray(2).endsWith("""" .""")) {// && !(lineArray(2).contains("""-""") || lineArray(2).contains("""–"""))) {    	
        val wordArray = lineArray(2).split(" ")
    	for (word <- wordArray) {    	  
    	  val currentWord = word.replaceAll("""["`´'%,.:;“()‘’#&+€$]""","")    	  
    	  if (!currentWord.matches("^\\d*$") && currentWord != "" && currentWord.length > 1) {    	    
    	    wordsListBuffer += currentWord
    	  }
    	}
      }
      
      //if (i % 1000 == 0) println (i + " lines processed.")
      i += 1      
    }
            
    val uniqueWordList = wordsListBuffer.sorted.distinct
    
    for (word <- uniqueWordList) {      
      buffer.append(word + '\n')
    	    
	  if (j % 100000 == 0 && !buffer.isEmpty) {
        appendToFile(output, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
    	  
        println(j + " words processed.")
      }
	  
	  j += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString.dropRight(1))	
    }
  }
  
  def filterOccs(wordsFile: String, occsFile: String, output: String) {
    
    implicit val codec = Codec("iso-8859-1")    
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)
    
    var buffer = new StringBuilder
    val loop = new Breaks
    var i = 1
    var j = 1
    
    for (word <- Source.fromFile(wordsFile).getLines()) {    
      var canStop = false
    
      loop.breakable {
        for (occsLine <- Source.fromFile(occsFile).getLines()) { 
          val occsUri = occsLine.split('\t')(1) 
          if (word.charAt(0) == occsUri.charAt(0)) {
            if (word == occsUri) {
              println("uri = " + word)
              println("occsUri = " + occsUri)
              buffer.append(occsLine + '\n')
            
              if (j % 25 == 0 && !buffer.isEmpty) {
                println ("Relation file current line = " + i)
                appendToFile(output, buffer.toString.dropRight(1))
    	        buffer.delete(0, buffer.length)
                buffer = new StringBuilder
              }
      
              j += 1
              canStop = true
              //loop.break
            }
          } else if (word.charAt(0) < occsUri.charAt(0) || canStop == true) {
            loop.break
          }
        }
      }
      
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }
  }
  
  def remove(aWord: String, list: List[String]) = list diff List(aWord)
  
  def filterWordsList(wordsList: String, glbOccs: String, filteredWordsList: String) {
    
    implicit val codec = Codec("iso-8859-1")    
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)
    
    val loop = new Breaks
    
    var buffer = new StringBuilder
    var j = 1
    var i = 1
    var z = 1
    
    var wordsFinalList = Source.fromFile(wordsList).getLines().toList 
    var auxWordsList = Source.fromFile(wordsList).getLines().toList
    
    
    for (word <- auxWordsList) {
      if (!isUpperCase(word.charAt(0))) {
        wordsFinalList = remove(word, wordsFinalList)
      }
      
      z += 1
    }
    
    for (word <- wordsFinalList) {
      var canStop = false
      
      loop.breakable {
        for (occsLine <- Source.fromFile(glbOccs).getLines()) {
          val occsUri = occsLine.split('\t')(1) 
          if (word.charAt(0) == occsUri.charAt(0)) {
            if (word == occsUri) {
              canStop = true
            }
          } else if (word.charAt(0) < occsUri.charAt(0) || canStop == true) {
            loop.break
          }
        }
      }
      
      if (canStop == false) {
        buffer.append(word + '\n')
        
        if (j % 25 == 0 && !buffer.isEmpty) {
          println ("Words file current line = " + i)
          appendToFile(filteredWordsList, buffer.toString.dropRight(1))
	      buffer.delete(0, buffer.length)
          buffer = new StringBuilder
        }
  
        j += 1
      }
      
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(filteredWordsList, buffer.toString)	
    }
  }
  
  def complementEntities(videosFile: String, dbpediaFile: String, outputDir: String, glbOccs: String) {
    generateWordList(videosFile, outputDir + "words_list")   
    filterWordsList(outputDir + "words_list", glbOccs, outputDir + "filtered_words_list")
    filterOccs(outputDir + "filtered_words_list", dbpediaFile, outputDir + "videos_occs.tsv")
  }
}