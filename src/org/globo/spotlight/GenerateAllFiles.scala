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
import org.apache.commons.io
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
      createDir(base_dir + "/turtle_files/")      
      createDir(base_dir + "/output_ori/")
      createDir(base_dir + "/TDB/")
            
      val file = new File(inputDir)      
      if(file.isDirectory()) { 
		if(file.list().length == 0){
		  println("Please put all the .ttl extension files from the globo dataset inside the folder " + inputDir)
		  System.exit(1)
		}
      } else {
        println("Please use a valid input directory.")
        System.exit(1)
      }
               
      // Clean the current TDB folder so we don't receive a null pointer exception
      org.apache.commons.io.FileUtils.cleanDirectory(new java.io.File(tdbDir))
    
      // Combines all globo files into one .ttl
      //generateDataset(inputDir, inputDir + turtleFile)
      // Converts the dataset to iso
      //convertFormat(inputDir + "/" + turtleFile, inputDir + "/test.ttl")      
    
      //val globoModel = loadFileToJena(inputDir + turtleFile, tdbDir)      
      //LabelsNT.generateLabelsJena(globoModel, outputDir + "labels_globo.nt")          
      
      /*val it = globoModel.listStatements()
      LabelsNT.generateLabelsNT(it, outputDir + "labels_globo.nt")
    
      // A query to find if the subject from the main language has any types in the instance types triples file
      println("Querying for all entries with the types...")
      val typesQuery = buildQueryRDFTypes()
      val typesResults = executeQuery(typesQuery, globoModel)
      println("Done.")
      InstanceTypesNT.generateInstanceTypesNT(typesResults, outputDir + "instance_types_globo.nt")
    
      // Generate the redirects file
      Redirects.generateRedirects(it, outputDir)
      
      // Generate the context file
      Context.generateContext(it, outputDir)    */  
    
      // Generate the final XML, the dump itself
      //Wiki.generateWikiHTML(outputDir + "context_globo.ttl", outputDir + "globo_dump.xml")
      //Wiki.generateWikiJena(outputDir + "permalinks_globo.ttl", outputDir + "globo_dump.xml", globoModel)
      
      // Generate the Globo DBpedia mapping
      //GloboToDbpedia.filterLabels(outputDir + "labels_globo.ttl", outputDir + "person_organization_location_types", outputDir + "filtered_labels_globo.ttl")
      //GloboToDbpedia.generateGlbDbMapping(outputDir + "filtered_labels_globo.ttl", outputDir + "sorted_labels_pt.nt", outputDir + "globo_map_dbpedia.nt")
      //GloboToDbpedia.generateGlbDbMapping2(outputDir + "labels_final.nt", outputDir + "globo_map_dbpedia.nt")
     
      // Get or tries to get only the correct mapping from the Globo -> Wikipedia mapping
      //GloboToDbpedia.getCorrectEntries(outputDir + "sorted_globo_map_dbpedia.nt", outputDir + "globo_final_map_dbpedia.nt")      
      
      // Generates the first utf globo_occs
      // sort -t$'\t' -k2 occs.tsv > occs.uriSorted.tsv 
      //GloboToDbpedia.filterOccs(outputDir + "globo_final_map_dbpedia.nt", "E:/Spotlight/data/output/pt/occs_uriSorted.tsv" , outputDir + "occs_globo_0.tsv")      
      //fixOccs(outputDir + "occs_globo_0.tsv", outputDir + "occs_globo_1.tsv")
      
      // Generates the first video based globo occs
      //fixOccs("E:/Spotlight/data/output/pt/occs_uriSorted.tsv", outputDir + "final_occs_uriSorted.tsv")
      //VideosNT.complementEntities(outputDir + "videos_dataset.ttl", outputDir + "final_occs_noNumber_uriSorted.tsv", outputDir, outputDir + "occs_globo_1.tsv")
            
      // Generates the final occs with extra column
      // cat occs_globo_1.tsv videos_occs.tsv > occs_globo_2.tsv
      // sort -u occs_globo_2.tsv > occs_globo_2_sorted.tsv
      
      // Because of encoding problems the sort might not filter duplicates, use awk also
      // awk -F '\t' '!x[$1]++' occs_globo_2_sorted.tsv > sorted_occs_globo_2.tsv
      
      // cat occs.tsv sorted_occs_globo_2.tsv > test.tsv
      // sort test.tsv > sorted_test.tsv
      GloboToDbpedia.addColumnToOccs(outputDir + "sorted_test.tsv", outputDir + "occs_globo_3.tsv")
    }
  }
}