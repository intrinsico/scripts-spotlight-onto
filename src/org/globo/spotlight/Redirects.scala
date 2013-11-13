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

import com.hp.hpl.jena.rdf.model.StmtIterator
import org.globo.spotlight.util.FileUtils._

object Redirects {
  private val PREDICATE_LABEL = "http://www.w3.org/2002/07/owl#sameAs"
  private val REDIRECTS_PREFIX = "http://semantica.globo.com/wikiPageRedirects"  
  
  def generateRedirectsFile(it: StmtIterator, outputFile: String) {
    var buffer = new StringBuilder            
    //appendToFile(output, buffer)

    var i = 0
    println("Creating redirects file...")
    while (it.hasNext()) {

      if (i % 10000 == 0) println (i + " lines processed.")
      
      val stmt = it.nextStatement()
      val subject = stmt.getSubject()
      val predicate = stmt.getPredicate()
      val obj = stmt.getObject()      

      if (predicate.toString().equals(PREDICATE_LABEL)) {
        buffer.append("<%s> <%s> <%s> .\n".format(subject, REDIRECTS_PREFIX, obj))
      }
      
      if (i != 0 && i % 1000000 == 0 && !buffer.isEmpty) {
        appendToFile(outputFile, buffer.toString.dropRight(1))
        buffer.delete(0, buffer.length)
        buffer = new StringBuilder
      } 
      
      i += 1
    }
    appendToFile(outputFile, buffer.toString.dropRight(1))
  }
  
  def generateRedirects(it: StmtIterator, outputDir: String) {    
    generateRedirectsFile(it, outputDir + "redirects_globo.ttl")
  }
}