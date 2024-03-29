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

import com.hp.hpl.jena.rdf.model.{ModelFactory, StmtIterator, Model}
import com.hp.hpl.jena.tdb.TDBFactory
import java.io._
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import com.hp.hpl.jena.query.{QueryExecutionFactory, QueryFactory, ResultSet}

object JenaUtils {
  def createModel(aDirectory: String): Model = {
    val dataset = TDBFactory.createDataset(aDirectory)
    dataset.getDefaultModel
  } 
  
  // Executes a select query over the datasets
  def executeQuery(aQuery: String, aModel: Model): ResultSet = {
    val query = QueryFactory.create(aQuery)
    val qexec = QueryExecutionFactory.create(query, aModel)
    qexec.execSelect()
  }
  
  def buildQueryRDFLabelsWithTypesPOP(): String = {      
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + '\n' +
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + '\n' +    
    "PREFIX place: <http://semantica.globo.com/place/>" + '\n' +
    "PREFIX person:	<http://semantica.globo.com/person/>" + '\n' +
    "PREFIX organization: <http://semantica.globo.com/organization/>" + '\n' +
    "PREFIX base: <http://semantica.globo.com/base/>" + '\n' +
    "PREFIX esportes: <http://semantica.globo.com/esportes/>" + '\n' +
    "SELECT ?class ?lugar ?pessoa ?org ?o ?nc ?apel" + '\n' +    
    "WHERE {" +
      "{ " +
        "?class rdfs:subClassOf* place:Place . " +
        "?lugar rdf:type ?class . " +
        "?lugar rdfs:label ?o . " +
        "OPTIONAL { ?lugar base:nome_completo ?nc . } . " +        
      "} " +
      "UNION " +
      "{ " +
        "?class rdfs:subClassOf* person:Person . " +
        "?pessoa rdf:type ?class . " +
        "?pessoa rdfs:label ?o . " +
        "OPTIONAL { ?pessoa base:nome_completo ?nc . } . " +
        "OPTIONAL { ?atleta base:desempenhado_por ?pessoa . ?atleta esportes:nome_popular_sde ?apel . } . " +
      "} " +
      "UNION " +
      "{ " +
        "?class rdfs:subClassOf* organization:Organization . " +
        "?org rdf:type ?class . " +
        "?org rdfs:label ?o . " +
        "OPTIONAL { ?org base:nome_completo ?nc . } . " +        
      "} " +      
    "}"
  }

  def buildQueryRDFPersonType(): String = {
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + '\n' +
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + '\n' +
    "PREFIX person:	<http://semantica.globo.com/person/>" + '\n' +
    "SELECT ?class ?pessoa" + '\n' +
    "WHERE {" +
      "{ " +
        "?class rdfs:subClassOf* person:Person . " +
        "?pessoa rdf:type ?class . " +
      "} " +
    "}"
  }

  def buildQueryRDFPlaceType(): String = {
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + '\n' +
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + '\n' +
    "PREFIX place: <http://semantica.globo.com/place/>" + '\n' +
    "SELECT ?class ?lugar" + '\n' +
    "WHERE {" +
      "{ " +
        "?class rdfs:subClassOf* place:Place . " +
        "?lugar rdf:type ?class . " +
      "} " +
    "}"
  }

  def buildQueryRDFOrgType(): String = {
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + '\n' +
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + '\n' +
    "PREFIX organization: <http://semantica.globo.com/organization/>" + '\n' +
    "SELECT ?class ?org" + '\n' +
    "WHERE {" +
      "{ " +
        "?class rdfs:subClassOf* organization:Organization . " +
        "?org rdf:type ?class . " +
      "} " +
    "}"
  }

  def buildQueryRDFAllTypes(): String = {    
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + '\n' +    
    "SELECT ?s ?o " + '\n' +
    "WHERE {?s rdf:type ?o}"    
  }
  
  def buildQueryAllLabels(): String = {
    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + '\n' +    
    "SELECT ?s ?o " + '\n' +
    "WHERE {?s rdfs:label ?o}"
  }
  
  def loadFileToJena(turtleFile: String, tdbPath: String): Model = {
    val in = new BufferedInputStream(new FileInputStream(turtleFile))    
    val charsetDecoder = Charset.forName("utf-8").newDecoder()
    
	  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE)
	  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE)
	  val inputReader = new InputStreamReader(in, charsetDecoder)
    
	  // Create a model to represent the Globo dataset
	  println("Reading file into Jena")
    val fbTdbStore = createModel(tdbPath)	
    fbTdbStore.read(inputReader, null, "TURTLE")
    fbTdbStore
  }
  
  def queryUri(uri: String): String = {           
    "SELECT ?s ?p ?o" + '\n' +
    "WHERE {" + uri + "?p ?o}"    
  }
}