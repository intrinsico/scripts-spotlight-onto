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

import scala.io.Source
import scala.collection.mutable.ListBuffer
import scala.util.control._
import org.globo.spotlight.util.StringUtils._
import org.jsoup.Jsoup
import java.io._

object GloboToDbpedia {

  val estadosList = List[(String, String)](
    ("Acre", "AC"),
    ("Alagoas", "AL"),
    ("Amapá", "AP"),
    ("Amazonas", "AM"),
    ("Bahia", "BA"),
    ("Ceará",	"CE"),
    ("Distrito Federal", "DF"),
    ("Espírito Santo", "ES"),
    ("Goiás", "GO"),
    ("Maranhão", "MA"),
    ("Mato Grosso", "MT"),
    ("Mato Grosso do Sul", "MS"),
    ("Minas Gerais", "MG"),
    ("Pará", "PA"),
    ("Paraíba", "PB"),
    ("Paraná", "PR"),
    ("Pernambuco", "PE"),
    ("Piauí", "PI"),
    ("Rio de Janeiro", "RJ"),
    ("Rio Grande do Norte",	"RN"),
    ("Rio Grande do Sul",	"RS"),
    ("Rondônia", "RO"),
    ("Roraima", "RR"),
    ("Santa Catarina", "SC"),
    ("São Paulo", "SP"),
    ("Sergipe", "SE"),
    ("Tocantins",	"TO"))
  
  def generateStatesList(statesFile: String): List[(String, String)] = {
    val auxStatesList = new ListBuffer[(String, String)]
    for (state <- Source.fromFile(statesFile).getLines()) {
      println("opa")
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
  
  def getCorrectEntries(glbDbMapFile: String, output: String) {
    var i = 1
    var j = 1
    var corrects = 0
    var incorrects = 0
    val loop = new Breaks
    
    val MIN_INDEX = 0
    val MAX_INDEX = 25000

    // Generate the tuples list of states we are going to need, Complete name and Abbreviation
    val correctEntriesStream = new PrintStream(new File(output))
    println("Getting the correct entries from the Globo to DBpedia mapping...")
    loop.breakable {
      for (line <- Source.fromFile(glbDbMapFile).getLines().drop(1)) {
        if (i > MIN_INDEX && i <= MAX_INDEX) {
          val lineArray = line.split(' ')
          val fcUriTag = lineArray(0).split('/')
          val fcUri = fcUriTag(fcUriTag.length-1).dropRight(1)
          val tc = lineArray(2).replaceAll("[<>]","")

          try {
            val documentText = removeDiacriticalMarks(Jsoup.connect(tc).get().text()).toLowerCase
            var isCorrect = true

            val uriArray = {
              fcUri match {
                case x if lineArray(0).contains("Bairro") => tokenizeCityUri(estadosList, "Bairro", x, shouldDrop = false)
                case x if x.contains("Cidade") => tokenizeCityUri(estadosList, "Bairro", x, shouldDrop = true)
                case x if x.contains("Canal") => tokenizeUri("Canal", x, shouldDrop = true)
                case x if x.contains("Organizacao") => tokenizeUri("Organizacao", x, shouldDrop = true)
                case x if x.contains("Pais") => tokenizeUri("Pais", x, shouldDrop = true)
                case x if x.contains("Pessoa") => tokenizeUri("Pessoa", x, shouldDrop = true)
                case x if x.contains("Programa") => tokenizeUri("Programa", x, shouldDrop = true)
                case x if x.contains("UFC") => tokenizeUri("UFC", x, shouldDrop = true)
                case x if x.contains("AfiliadaGlobo") => tokenizeUri("AfiliadaGlobo", x, shouldDrop = true)
                case x if x.contains("ArtistaIndividual") => tokenizeUri("ArtistaIndividual", x, shouldDrop = true)
                case x if x.contains("Emissora") => tokenizeUri("Emissora", x, shouldDrop = true)
                case x if x.contains("EntidadeEmpresarial") => tokenizeUri("EntidadeEmpresarial", x, shouldDrop = true)
                case x if x.contains("EscolaDeSamba") => tokenizeUri("EscolaDeSamba", x, shouldDrop = true)
                case x if x.contains("MontadoraDeVeiculos") => tokenizeUri("MontadoraDeVeiculos", x, shouldDrop = true)
                case x if x.contains("PartidoPolitico") => tokenizeUri("PartidoPolitico", x, shouldDrop = true)
                case x if x.contains("Politico") => tokenizeUri("Politico", x, shouldDrop = true)
                case x if lineArray(0).contains("Atleta") => tokenizeUri("Atleta", x, shouldDrop = true)
                case x if lineArray(0).contains("ScoutAtleta") => tokenizeUri("ScoutAtleta", x, shouldDrop = true)
                case x if lineArray(0).contains("PersonalidadeDeTV") => tokenizeUri("PersonalidadeDeTV", x, shouldDrop = true)
                case x if x.contains("UF") => tokenizeUFUri(estadosList, x)
                case x if x.contains("InstituicaoEnsino") => tokenizeIEUri(x)
                case _ => Array[String]()
              }
            }

            if (!uriArray.isEmpty) {
              loop.breakable {
                for (item <- uriArray) {
                  if (!documentText.contains(item.toLowerCase)) {
                    isCorrect = false
                    incorrects += 1
                    loop.break()
                  }
                }
              }

              if (isCorrect) {
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
          loop.break()
        }
        i += 1
      }
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
        dbThirdColumn.find((x: (String, Int)) => x._1 == glbLineArray(2)) match {
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