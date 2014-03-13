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
                       
      val inputDir = base_dir + "/turtle_files2/"
      val outputDir = base_dir + "/output_ori/"
      val tdbDir = base_dir + "/TDB/"
      
      // Creates all folders we are going to need
      createDir(base_dir)
      createDir(inputDir)      
      createDir(outputDir)
      createDir(tdbDir)
            
      //val file = new File(inputDir)      
      //if(file.isDirectory()) { 
	  //	if(file.list().length == 0){
	  //	  println("Please put all the .ttl extension files from the globo dataset inside the folder " + inputDir)
	  //	  System.exit(1)
	  //	}
      //} else {
      //  println("Please use a valid input directory.")
      //  System.exit(1)
      //}
               
      // Clean the current TDB folder so we don't receive a null pointer exception
      cleanDirectory(new java.io.File(tdbDir))
    
      // Combines all globo files into one .ttl
      //generateDataset(inputDir, inputDir + turtleFile)      
    
      ////////////////////////////////////////////////////// LABELS ///////////////////////////////////////////////
      val globoModel = loadFileToJena(inputDir + turtleFile, tdbDir)                  
      val labelsQuery = buildQueryRDFLabelsWithTypesPOP()
      val labelsResults = executeQuery(labelsQuery, globoModel)
      println("Done.")
      LabelsNT.generateLabelsTSV(labelsResults, outputDir + "surfaceForms-globo.tsv")
      //LabelsNT.generateLabelsNT(labelsResults, outputDir + "labels_globo.nt")     
      
      // Compress the final file
      // bzip2 -k labels_pt.nt
      
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////
      ///////////////////////////////////////////////// INSTANCE TYPES ////////////////////////////////////////////
      /*
      // A query to find if the subject from the main language has any types in the instance types triples file
      println("Querying for all entries with the types...")
      val typesQuery = buildQueryRDFAllTypes()
      val typesResults = executeQuery(typesQuery, globoModel)
      println("Done.")
      
      // Generating the instance types file itself
      InstanceTypesNT.generateInstanceTypesNT(typesResults, outputDir + "instance_types_globo.nt")
      
      // Adds the DBpedia types of Person, Location and Organisation to the Globo respective entities
      // awk -F ' ' '$3 ~ /Pessoa/{print}' instance_types_globo.ttl > pessoa.ttl
      // awk -F ' ' '{print $1" "$2" <http://dbpedia.org/ontology/Person> ."}' pessoa.ttl > pessoa2.ttl
      // awk -F ' ' '$3 ~ /Organizacao/{print}' instance_types_globo.ttl > organizacao.ttl
      // awk -F ' ' '{print $1" "$2" <http://dbpedia.org/ontology/Organisation> ."}' organizacao.ttl > organizacao2.ttl
      // awk -F ' ' '$3 ~ /Endereco/{print}' instance_types_globo.ttl >> lugar.ttl
      // awk -F ' ' '$3 ~ /Regiao/{print}' instance_types_globo.ttl >> lugar.ttl
      // awk -F ' ' '$3 ~ /Cidade/{print}' instance_types_globo.ttl >> lugar.ttl
      // awk -F ' ' '$3 ~ /Pais/{print}' instance_types_globo.ttl >> lugar.ttl
      // awk -F ' ' '$3 ~ /UF/{print}' instance_types_globo.ttl >> lugar.ttl
      // awk -F ' ' '$3 ~ /AcidenteGeografico/{print}' instance_types_globo.ttl >> lugar.ttl
      // awk -F ' ' '$3 ~ /Bairro/{print}' instance_types_globo.ttl >> lugar.ttl                        
      // awk -F ' ' '{print $1" "$2" <http://dbpedia.org/ontology/Place> ."}' lugar.ttl > lugar2.ttl
      // cat pessoa2.ttl organizacao2.ttl lugar2.ttl >> instance_types_globo.ttl
      // Sort when combining with the dbpedia types file!            
      
      // Generate the context file*/
      //val it = globoModel.listStatements()
      //Context.generateContext(it, outputDir)  
    
      // Generate the final XML, the dump itself
      //Wiki.generateWikiHTML(outputDir + "context_globo.ttl", outputDir + "globo_dump.xml")
      //Wiki.generateWikiJena(outputDir + "permalinks_globo.ttl", outputDir + "globo_dump.xml", globoModel)
      
      // Generate the Globo DBpedia mapping      
      //GloboToDbpedia.generateGlbDbMapping(outputDir + "labels_globo.nt", outputDir + "sorted_labels_pt.nt", outputDir + "globo_map_dbpedia.nt")      
     
      // Get or tries to get only the correct mapping from the Globo -> Wikipedia mapping
      // sort globo_map_dbpedia.nt > sorted_globo_map_dbpedia.nt
      //GloboToDbpedia.getCorrectEntries(outputDir + "sorted_globo_map_dbpedia.nt", outputDir + "globo_final_map_dbpedia.nt")                 
    }
  }
}