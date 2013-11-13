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

object GenerateAllFiles {  

  def main(args: Array[String]) {
    if (args.length != 2) {
      println("Wrong number of arguments!")
      System.exit(1)
    } else {    
      // The root folder for the conversion process
      val base_dir = args(0)
      // The Globo dataset
      val turtleFile = args(1)
      
      // Creates all the folder we are going to need
      createDir(base_dir)
      val inputDir = base_dir + "/turtle_files/"
      createDir(inputDir)
      val outputDir = base_dir + "/output/"
      createDir(outputDir)
      val tdbDir = base_dir + "/TDB/"
      createDir(tdbDir)
      
      //TODO: download the Globo files into the turtl_files folder, and extract them without the .graph files
      val file = new File(inputDir)      
      if(file.isDirectory()) { 
		if(file.list().length == 0){
		  println("Please put all the .ttl extension files from the globo dataset inside the folder " + inputDir)
		  System.exit(1)
		}
      } else {
        System.exit(1)
      }
    
      // Clean so we dont receive a null pointer exception
      org.apache.commons.io.FileUtils.cleanDirectory(new java.io.File(base_dir + "TDB"))
    
      generateDataset(inputDir, inputDir + turtleFile)
    
      val globoModel = loadFileToJena(inputDir + turtleFile, tdbDir)
      val it = globoModel.listStatements()
      LabelsNT.generateLabelsNT(it, outputDir + "labels_globo.nt")
    
      // A query to find if the subject from the main language has any types in the instance types triples file
      println("Querying for all entries with the types...")
      val typesQuery = buildQueryRDFTypes()
      val typesResults = executeQuery(typesQuery, globoModel)
      println("Done.")
      InstanceTypesNT.generateInstanceTypesNT(typesResults, outputDir + "instance_types_globo.nt")
    
      // Generate the context file
      Context.generateContext(it, outputDir)
      Redirects.generateRedirects(it, outputDir)
    
      // Generate the final XML, the dump itself
      Wiki.generateWiki(outputDir + "context_globo.ttl", outputDir + "globo-latest-pages-articles.xml")  
    }
  }
}