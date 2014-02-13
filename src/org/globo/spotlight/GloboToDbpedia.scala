package org.globo.spotlight

import scala.io.Source
import scala.math.Ordering.Char._
import scala.collection.mutable.ListBuffer
import scala.util.control._
import scala.collection.mutable.ArrayBuffer
import org.globo.spotlight.util.FileUtils._
import org.globo.spotlight.util.StringUtils._
import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import java.io.IOException
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.nio.charset.Charset

object GloboToDbpedia {
  def addColumnToOccs(occsFile: String, output: String) {
    implicit val codec = Codec("utf-8")    
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    var buffer = new StringBuilder
    var i = 1 
    var auxLine = Source.fromFile(occsFile).getLines().next()
    var auxLineArray = auxLine.split('\t')
    var auxOffset = auxLineArray(auxLineArray.length-1)       
    
    for (line <- Source.fromFile(occsFile).getLines().drop(1)) {
      val lineArray = line.split("\t")
      if (lineArray(lineArray.length-1) == auxOffset) {
        buffer.append(auxLine + "\tG\n")
        auxLine = ""
        auxOffset = ""
      } else {
        if (auxOffset != "") {
          buffer.append(auxLine + "\tDB\n")          
        } 
        auxLine = line
        auxOffset = lineArray(lineArray.length-1)
      }
      
      if (i % 10000 == 0 && !buffer.isEmpty) {
        appendToFile(output, buffer.toString.dropRight(1))
	    buffer.delete(0, buffer.length)
	    buffer = new StringBuilder
	  
	    println(i + " lines processed.")
      }
      
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }

    println("Done.")
  }     
  
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
  
  def filterOccs(glbToDbFile: String, occsFile: String, output: String) {
    
    implicit val codec = Codec("UTF-8")    
    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    var buffer = new StringBuilder
    val loop = new Breaks
    var i = 1
    var j = 1
    
    for (line <- Source.fromFile(glbToDbFile).getLines()) {      
      val tcArray = line.split(' ')(2).split('/')
      val uri = tcArray(tcArray.length-1).dropRight(1)
      var canStop = false           
      
      loop.breakable {
        for (occsLine <- Source.fromFile(occsFile).getLines()) { 
          val occsUri = occsLine.split('\t')(1) 
          if (uri.charAt(0) == occsUri.charAt(0)) {
            if (uri == occsUri) {
              println("uri = " + uri)
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
            }
          } else if (uri.charAt(0) < occsUri.charAt(0) || canStop == true) {
            loop.break
          }
        }
      }
      
      if (i % 25 == 0) println(i + " lines processed.")
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }
  }
  
  def getCorrectCities(fcUri: String, tc: String) {
    try {
      val documentText = removeDiacriticalMarks(Jsoup.connect(tc).get().text()).toLowerCase()
      var isCorrect = true                             
      
      val uriArray = fcUri.split("_").dropRight(1)
      for (item <- uriArray) {
        if (item != "InstituicaoEnsino") {          
          if (!documentText.contains(item.toLowerCase())) {
            isCorrect = false            
          }                       
        }
      }                              
    } catch {
      case e: IOException => println("An error occurred while parsing this page!")
    }
  }
  
  def generateStatesList(statesFile: String): List[(String, String)] = {
    val auxStatesList = new ListBuffer[(String, String)]
    for (state <- Source.fromFile(statesFile).getLines()) {
      val auxTuple = state.split('\t')
      auxStatesList += Tuple2(auxTuple(0), auxTuple(1))
    }
    auxStatesList.toList
  }
  
  def getStateName(statesList: List[(String, String)], stateAbbrev: String): String = {    
    for (state <- statesList) {      
      if (state._2 == stateAbbrev) {        
        return state._1
      }
    }
    ""
  }
  
  def tokenizeUri(default: String, uri: String, shouldDrop: Boolean): Array[String] = {
    if (uri == default) {
      Array[String](default)
    } else {
      if (shouldDrop) {
        uri.replaceAll("-","_").split('_').drop(1)
      } else {
        uri.replaceAll("-","_").split('_')
      }
    }
  }
  
  def tokenizeUFUri(stateList: List[(String, String)], uri: String): Array[String] = {
    if (uri == "UF") {
      Array[String]("UF")
    } else {                    
      getStateName(stateList, uri.replaceAll("-","_").split('_').drop(1)(0)).split(" ")
    }
  }
  
  def tokenizeIEUri(uri: String): Array[String] = {
    if (uri == "InstituicaoEnsino") {
      Array[String]("InstituicaoEnsino")
    } else {                    
      uri.replaceAll("-","_").split('_').drop(1).dropRight(1)
    }
  }
  
  def tokenizeCityUri(stateList: List[(String, String)], default: String, uri: String, shouldDrop: Boolean): Array[String] = {
    val auxArray = uri.replaceAll("-","_").split('_')                
    val auxStateName = getStateName(stateList, auxArray(auxArray.length-1))                
    if (auxStateName != "") {
      if (shouldDrop) {
        auxArray.drop(1).dropRight(1) ++ auxStateName.split(" ")
      } else {
    	auxArray.dropRight(1) ++ auxStateName.split(" ")
      }
    } else {
      if (shouldDrop) {
        auxArray.drop(1).dropRight(1)
      } else {
        auxArray.dropRight(1)
      }
    }
  }
  
  def getCorrectEntries(glbDbMapFile: String, estadosFile: String, output: String) {
    var buffer = new StringBuilder
    var i = 1
    var j = 1
    var corrects = 0
    var incorrects = 0
    val loop = new Breaks
    
    val MIN_INDEX = 0
    val MAX_INDEX = 25000
    
    // Generate the tuples list of states we are going to need, Complete name and Abbreviation
    val estadosList = generateStatesList(estadosFile)
    
    for (line <- Source.fromFile(glbDbMapFile).getLines().drop(1)) {
      if (i > MIN_INDEX && i <= MAX_INDEX) {
        val lineArray = line.split(' ')
        val fcUriTag = lineArray(0).split('/')
        val fcUri = fcUriTag(fcUriTag.length-1).dropRight(1)                                      
        val tc = lineArray(2).replaceAll("[<>]","")
        
        try {
          val documentText = removeDiacriticalMarks(Jsoup.connect(tc).get().text()).toLowerCase()
          var isCorrect = true                             
          
          val uriArray = {
            fcUri match {
              case x if (lineArray(0).contains("Bairro")) => tokenizeCityUri(estadosList, "Bairro", x, false)
              case x if (x.contains("Cidade")) => tokenizeCityUri(estadosList, "Bairro", x, true)                 
              case x if (x.contains("Canal")) => tokenizeUri("Canal", x, true)                              
              case x if (x.contains("Organizacao")) => tokenizeUri("Organizacao", x, true)                
              case x if (x.contains("Pais")) => tokenizeUri("Pais", x, true)                
              case x if (x.contains("Pessoa")) => tokenizeUri("Pessoa", x, true)                
              case x if (x.contains("Programa")) => tokenizeUri("Programa", x, true) 
              case x if (x.contains("UFC")) => tokenizeUri("UFC", x, true)
              case x if (x.contains("AfiliadaGlobo")) => tokenizeUri("AfiliadaGlobo", x, true)
              case x if (x.contains("ArtistaIndividual")) => tokenizeUri("ArtistaIndividual", x, true)
              case x if (x.contains("Emissora")) => tokenizeUri("Emissora", x, true)
              case x if (x.contains("EntidadeEmpresarial")) => tokenizeUri("EntidadeEmpresarial", x, true)
              case x if (x.contains("EscolaDeSamba")) => tokenizeUri("EscolaDeSamba", x, true)
              case x if (x.contains("MontadoraDeVeiculos")) => tokenizeUri("MontadoraDeVeiculos", x, true)
              case x if (x.contains("PartidoPolitico")) => tokenizeUri("PartidoPolitico", x, true)
              case x if (x.contains("Politico")) => tokenizeUri("Politico", x, true)
              case x if (lineArray(0).contains("Atleta")) => tokenizeUri("Atleta", x, false)              
              case x if (lineArray(0).contains("ScoutAtleta")) => tokenizeUri("ScoutAtleta", x, false)     
              case x if (lineArray(0).contains("PersonalidadeDeTV")) => tokenizeUri("PersonalidadeDeTV", x, false)
              case x if (x.contains("UF")) => tokenizeUFUri(estadosList, x)            
              case x if (x.contains("InstituicaoEnsino")) => tokenizeIEUri(x)                                      
              case _ => Array[String]()
            }
          }
                          
          if (!uriArray.isEmpty) {            
            loop.breakable {
              for (item <- uriArray) {                
                if (!documentText.contains(item.toLowerCase())) {
                  isCorrect = false
                  incorrects += 1
                  loop.break
                }                                       
              }     
            }
          
            if (isCorrect == true) {
              corrects += 1
              buffer.append(line + '\n')
            
              if (j % 100 == 0 && !buffer.isEmpty) {
                println ("Globo file current line = " + i)
                appendToFile(output, buffer.toString.dropRight(1))
    	        buffer.delete(0, buffer.length)
    	        buffer = new StringBuilder
              }
      
              j += 1
            }    
          }
        } catch {
          case e: IOException => println("An error occurred while parsing this page!")
        }              
      } else if (i > MAX_INDEX) {
        println("Corrects = " + corrects + " and incorrects = " + incorrects)
        
        if (!buffer.isEmpty) {
          appendToFile(output, buffer.toString.dropRight(1))	
        }
        
        System.exit(1)      
      }
      i += 1
    }
    
    println("Corrects = " + corrects + " and incorrects = " + incorrects)
        
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString.dropRight(1))	
    }
  }
  
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
  
  def generateGlbDbMapping2(labelsFile: String, output: String) {
    var buffer = new StringBuilder
    var auxLine = ""
    var i = 1  
    var j = 1
      
    for (line <- Source.fromFile(labelsFile).getLines()) {
      if (i > 1) {
        val lineArray = line.split(' ')
        val auxLineArray = auxLine.split(' ')
        if (lineArray.length >= 3 && auxLineArray.length >= 3) {
          val label = lineArray(2).replaceAll(""""@pt""","").replaceFirst(""""""", "")                          
          val auxLabel = auxLineArray(2).replaceAll(""""@pt""","").replaceFirst(""""""", "")          
        
          if (levenshtein(label, auxLabel) <= 3) {          
          
            if (lineArray(0).split('/')(0).contains("globo") && auxLineArray(0).split('/')(0).contains("dbpedia")) {
              buffer.append(lineArray(0) + " <http://www.w3.org/2002/07/owl#sameAs> " + auxLineArray(0) + " .\n")
            } else if (lineArray(0).split('/')(0).contains("dbpedia") && auxLineArray(0).split('/')(0).contains("globo")) {
              buffer.append(auxLineArray(0) + " <http://www.w3.org/2002/07/owl#sameAs> " + lineArray(0) + " .\n")
            }
          
            if (j % 10000 == 0 && !buffer.isEmpty) {
        	  appendToFile(output, buffer.toString.dropRight(1))
        	  buffer.delete(0, buffer.length)
        	  buffer = new StringBuilder
            }
          
            j += 1
          }
                
    	  auxLine = line            
        } else {
          auxLine = line
        }
      }
      
      if (i % 100000 == 0) println (i + " lines processed.")
      i += 1
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString)	
    }

    println("Done.")
  }
  
  def generateGlbDbMapping3(glbLabelsFile: String, dbpediaLabelsFile: String, output: String) {
    
    var buffer = new StringBuilder
    var i = 1
    var j = 1
    
    val loop = new Breaks
    
    for (glbLine <- Source.fromFile(glbLabelsFile).getLines().drop(1)) {      
      val glbLineArray = glbLine.split(" ",3)      
      if (glbLineArray.length >= 3) {
        loop.breakable {
          for (line <- Source.fromFile(dbpediaLabelsFile).getLines().drop(1)) {            
            val dbLineArray = line.split(" ",3)
      
            if (dbLineArray.length >= 3 && (dbLineArray(2)(1).toLower.toInt >= 97 && dbLineArray(2)(1).toLower.toInt <= 122)) {	                     
              if (compare(glbLineArray(2)(1).toLower, dbLineArray(2)(1).toLower) == 0) {
                if (glbLineArray(2) == dbLineArray(2)) {
                  println("DB = " + dbLineArray(2))
                  println("GLB = " + glbLineArray(2))
                  println ("Globo file current line = " + i)
                  buffer.append(glbLineArray(0) + " <http://www.w3.org/2002/07/owl#sameAs> " + dbLineArray(0) + " .\n")
          
                  if (j % 10000 == 0 && !buffer.isEmpty) {	              
                    appendToFile(output, buffer.toString.dropRight(1))
        	        buffer.delete(0, buffer.length)
        	        buffer = new StringBuilder
                  }
          
                  j += 1
                  loop.break
                }
              } else if (compare(glbLineArray(2)(1).toLower, dbLineArray(2)(1).toLower) < 0) {                 
                loop.break
              }
            }            
          }          
        }
    
        if (i % 10000 == 0) println (i + " lines processed.")
        i += 1
      }
    }
  }
}