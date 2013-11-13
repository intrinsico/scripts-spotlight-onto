package org.aksw.spotlight.util

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
    
  def buildQueryRDFTypePerson(): String = {    
    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + '\n' +    
    "SELECT ?s ?o " + '\n' +
    "WHERE {?s rdf:type ?o}"    
  }
  
  def loadFileToJena(turtleFile: String, tdbPath: String): Model = {
    val in = new BufferedInputStream(new FileInputStream(turtleFile));
    //val charsetDecoder = Charset.forName("ISO-8859-1").newDecoder();
    val charsetDecoder = Charset.forName("UTF-8").newDecoder();
	charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
	charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
	val inputReader = new InputStreamReader(in, charsetDecoder);                 
    
	// Create a model to represent the Globo dataset
	println("Reading file into Jena")
    val fbTdbStore = createModel(tdbPath)	
    fbTdbStore.read(inputReader, null, "TURTLE")
    fbTdbStore
  }
}