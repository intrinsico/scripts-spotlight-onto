package org.aksw.spotlight

import org.aksw.spotlight.util.FileUtils._
import org.aksw.spotlight.util.JenaUtils._
import org.apache.commons.io

object GenerateAllFiles {

  val base_dir = "E:/globo_resources/"  
  val inputDir = base_dir + "turtle_files/"
  val turtleFile = "globo_dataset.ttl"
  val outputDir = base_dir + "output/"
  val tdbDir = base_dir + "TDB/"
  val LABEL = "http://www.w3.org/2000/01/rdf-schema#label"

  def main(args: Array[String]) {
    // Clean so we dont receive a null pointer exception
    org.apache.commons.io.FileUtils.cleanDirectory(new java.io.File(base_dir + "TDB"))
    
    //generateDataset(inputDir, inputDir + turtleFile)
    
    val globoModel = loadFileToJena(inputDir + turtleFile, tdbDir)
    val it = globoModel.listStatements()
    //LabelsNT.generateLabelsNT(it, outputDir + "labels_globo.nt")
    
    // A query to find if the subject from the main language has any types in the instance types triples file
    //println("Querying for all entries with the types...")
    //val typesQuery = buildQueryRDFTypePerson()
    //val typesResults = executeQuery(typesQuery, globoModel)
    //println("Done.")
    //InstanceTypesNT.generateInstanceTypesNT(typesResults, outputDir + "instance_types_globo.nt")
    
    //val it = null
    //Context.generateContext(it, outputDir)
    Redirects.generateRedirects(it, outputDir)
    //Wiki.generateWiki(outputDir + "context_globo.ttl", outputDir + "globo_dump.xml")
    
    //Wiki.generateWiki(turtleFile, outputDir + "globo-latest-pages-articles.xml")        
  }
}