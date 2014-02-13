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

import org.globo.spotlight.util.FileUtils._
import org.globo.spotlight.util.JenaUtils._
import java.util.Date
import java.io._
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import scala.io.Source
import com.hp.hpl.jena.rdf.model.{StmtIterator, Model}
import com.hp.hpl.jena.query.ResultSetFormatter

object LabelsNT {

  private val PREDICATE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label"
  private val OBJECT_LABEL = "http://xmlns.com/foaf/0.1/Person"
  private val INSERT_SPACE_UPPERCASE = "(\\p{Ll})(\\p{Lu})"         
    
  def generateLabelsJena(aModel: Model, output: String) {
    
    implicit val codec = Codec("iso-8859-1")    
    codec.onMalformedInput(CodingErrorAction.IGNORE)
    codec.onUnmappableCharacter(CodingErrorAction.IGNORE)
    
    val labelsQuery = buildQueryLabels()    
    val it = executeQuery(labelsQuery, aModel)
    val aList = ResultSetFormatter.toList(it)
    val aListIt = aList.iterator()

    
    var buffer = new StringBuilder
    
    var i = 1
    println("Creating labels file...")
    while (aListIt.hasNext()) {
    //while (it.hasNext()) {
      
      if (i % 100000 == 0) println (i + " lines processed.")
            
      //val next = it.next()
      val next = aListIt.next()
      val subject = next.get("s")      
      //val obj = new String (next.get("o").toString.getBytes("utf-8"))
      //val obj = new String (next.get("o").toString.getBytes("utf-8"), "iso-8859-1")
      val obj = next.get("o")
      //val obj = next.getLiteral("o").toString
      
      buffer.append("<" + subject + ">\t<http://www.w3.org/2000/01/rdf-schema#label>\t" + """"""" + obj + """"""" + "@pt .\n")           
            
      if (i % 1000000 == 0 && !buffer.isEmpty) {
        appendToFile(output, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      } 
      
      i += 1            
    }
    
    if (!buffer.isEmpty) {
      appendToFile(output, buffer.toString.dropRight(1))
    }
  }  
    
  def generateLabelsNT(it: StmtIterator, output: String) {             
    
    var buffer = new StringBuilder        
    buffer.append("# started ".concat(new Date().toString()).concat("\n"))
    //appendToFile(output, buffer)

    var i = 0
    println("Creating labels file...")
    while (it.hasNext()) {

      if (i % 100000 == 0) println (i + " lines processed.")
      
      val stmt = it.nextStatement()
      val subject = stmt.getSubject()
      val predicate = stmt.getPredicate()
      val obj = stmt.getObject()
      val language = "@pt" //stmt.getLanguage()??

      if (predicate.toString().equals(PREDICATE_LABEL)) {

        val surfaceForm = obj.toString().replace("@pt", "")
        
        val tmpString = "<%s> <%s> \"%s\"%s .\n".format(subject, predicate, surfaceForm, language)
        buffer.append(tmpString)

        val label = subject.toString().split("/")
        val name = label(label.size - 1).replaceAll(INSERT_SPACE_UPPERCASE, "$1 $2")

        if (!tmpString.contains("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, name, language))) {
          buffer.append("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, name, language))
          //appendToFile(output, buffer)
        }

        if (!tmpString.contains("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, label(label.size - 1), language))) {
          buffer.append("<%s> <%s> \"%s\"%s .\n".format(subject, predicate, label(label.size - 1), language))
          //appendToFile(output, buffer)
        }

      } else if (obj.toString().equalsIgnoreCase(OBJECT_LABEL)) {

        val label = subject.toString().split("/")
        val name = label(label.size - 1).replaceAll(INSERT_SPACE_UPPERCASE, "$1 $2")
        buffer.append("<%s> <%s> \"%s\"@en .\n".format(subject, PREDICATE_LABEL, name))        
      }
      
      if (i != 0 && i % 1000000 == 0 && !buffer.isEmpty) {
        appendToFile(output, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      } 
      
      i += 1
    }
    appendToFile(output, buffer.toString.dropRight(1))	
    //FileUtils.writeToFile(output, buffer.toString)

    println("Done.")
  }    
}