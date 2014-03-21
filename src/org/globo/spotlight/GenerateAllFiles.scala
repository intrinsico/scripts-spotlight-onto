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
import org.apache.commons.io.FileUtils._
import java.io._
import scala.io.Source

object GenerateAllFiles {    
  
  def main(args: Array[String]) {
    if (args.length != 2) {
      println("Wrong number of arguments!")
      System.exit(1)
    } else {    
      // The root folder for the conversion process
      val base_dir = args(0)
      // The Globo dataset file. All the base files concatenated without using graph files
      val turtleFile = args(1)
                       
      val inputDir = base_dir + "/turtle_files/"
      val outputDir = base_dir + "/output/"
      val tdbDir = base_dir + "/TDB/"
            
      val file = new File(inputDir)
      if(file.isDirectory) {
	  	  if(file.list().length == 0){
	  	    println("Please put all the .ttl extension files from the globo dataset inside the folder " + inputDir)
	  	    System.exit(1)
	  	  }
      } else {
        println("Please use a valid input directory.")
        System.exit(1)
      }
               
      // Clean the current TDB folder so we don't receive a null pointer exception
      cleanDirectory(new java.io.File(tdbDir))
    
      // Combines all globo files into one .ttl
      //generateDataset(inputDir, inputDir + turtleFile)

      // Generates the Globo model so we can search for entities
      //val globoModel = loadFileToJena(inputDir + turtleFile, tdbDir)

      ////////////////////////////////////////////////////// LABELS ///////////////////////////////////////////////
      //val labelsQuery = buildQueryRDFLabelsWithTypesPOP()
      //var labelsResults = executeQuery(labelsQuery, globoModel)
      //LabelsNT.generateLabelsTSV(labelsResults, outputDir + "surfaceForms-fromLabels-globo.tsv")
      //labelsResults = executeQuery(labelsQuery, globoModel)
      //LabelsNT.generateLabelsNT(labelsResults, outputDir + "labels_globo.nt")
      
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////
      ///////////////////////////////////////////////// INSTANCE TYPES ////////////////////////////////////////////

      // A query to find the types of all entities with types Person, Location and Organization
      //val placeTypeQuery = buildQueryRDFPlaceType()
      //val placeTypeResults = executeQuery(placeTypeQuery, globoModel)
      //val personTypeQuery = buildQueryRDFPersonType()
      //val personTypeResults = executeQuery(personTypeQuery, globoModel)
      //val orgTypeQuery = buildQueryRDFOrgType()
      //val orgTypeResults = executeQuery(orgTypeQuery, globoModel)
      
      // Generating the instance types file itself
      //InstanceTypesNT.generateInstanceTypesNT(placeTypeResults, personTypeResults, orgTypeResults, outputDir + "instance_types_globo.nt")
      
      // Generate the context file
      //val it = globoModel.listStatements()
      //Context.generateContext(it, outputDir)  
    
      // Generate the final XML, the dump itself
      //Wiki.generateWikiHTML(outputDir + "context_globo.ttl", outputDir + "globo_dump.xml")

      // Get Globo titles so we can save the full URIs from the Globo resources when saving the occs file
      //Context.generateContextTitles(outputDir + "permalinks_globo.ttl", outputDir + "globo_titles.tsv")

      // Generate the oocs file
      //ExtractOccsFromGlobo.saveOccsFile(outputDir + "globo_dump.xml", outputDir + "globo_titles.tsv", outputDir + "occs_globo.tsv")
      //ExtractOccsFromGlobo.saveTSVFromOccs(outputDir + "occs_globo.tsv", outputDir + "surfaceForms-fromOccs-globo.tsv")

      // Generate the Globo DBpedia mapping      
      //GloboToDbpedia.generateGlbDbMapping(outputDir + "labels_globo.nt", outputDir + "labels_pt.nt", outputDir + "globo_map_dbpedia.nt")
     
      // Get or tries to get only the correct mapping from the Globo -> Wikipedia mapping
      GloboToDbpedia.getCorrectEntries(outputDir + "globo_map_dbpedia.nt", outputDir + "globo_final_map_dbpedia.nt")
    }
  }
}