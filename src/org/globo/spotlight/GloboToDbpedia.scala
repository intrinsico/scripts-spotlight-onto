package org.globo.spotlight

import scala.io.Source
import scala.math.Ordering.Char._
import scala.collection.mutable.ListBuffer
import scala.util.control._
import scala.collection.mutable.ArrayBuffer
import scala.io.Codec
import org.globo.spotlight.util.FileUtils._
import org.globo.spotlight.util.StringUtils._
import org.jsoup.Jsoup
import org.jsoup.Connection.Method
import java.io.IOException
import java.nio.charset.CodingErrorAction
import java.nio.charset.Charset
import java.io._

object GloboToDbpedia {
    
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
    var i = 1
    var j = 1
    var corrects = 0
    var incorrects = 0
    val loop = new Breaks
    
    val MIN_INDEX = 0
    val MAX_INDEX = 25000
    
    // Generate the tuples list of states we are going to need, Complete name and Abbreviation
    val estadosList = generateStatesList(estadosFile)
    val correctEntriesStream = new PrintStream(new File(output))
    println("Getting the correct entries from the Globo to DBpedia mapping...")
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
              correctEntriesStream.println(line)              
            
              if (j % 100 == 0) {
                println ("Globo file current line = " + i)                
              }
      
              j += 1
            }    
          }
        } catch {
          case e: IOException => println("An error occurred while parsing this page!")
        }              
      } else if (i > MAX_INDEX) {
        println("Done .")
        println("Corrects = " + corrects + " and incorrects = " + incorrects) 
        return             
      }
      i += 1
    }
    
    println("Done .")
    println("Corrects = " + corrects + " and incorrects = " + incorrects)         
  }
  
  def generateGlbDbMapping(glbLabelsFile: String, dbpediaLabelsFile: String, output: String) {        
    var i = 1
    var j = 1
        
    val dbFirstColumn = new ListBuffer[String]
    val dbSecondColumn = new ListBuffer[String]
    val dbThirdColumn = new ListBuffer[(String, Int)]
    
    var z = 0
    val mappingStream = new PrintStream(new File(output))
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
            mappingStream.println(glbLineArray(0) + " <http://www.w3.org/2002/07/owl#sameAs> " + dbFirstColumn(x._2) + " .")
	          
            if (j % 100 == 0) {
              println ("Globo file current line = " + i)              
            }      
            j += 1
          }
          case None =>   
        }
                 
        if (i % 10000 == 0) println (i + " lines processed.")
        i += 1
      }
    }

    println("Done.")
  }
}