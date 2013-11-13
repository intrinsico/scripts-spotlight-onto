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

import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import scala.util.control.Breaks._
import java.util.Properties
import java.net.URLEncoder
import scala._
import java.util

class SparqlUtils {

  val SPARQL_QUERY = "select * where {<%s> ?p ?o FILTER (REGEX(STR(?p), 'http://www.w3.org/2002/07/owl#sameAs'))}"
  //val SPARQL_QUERY = "select * from SPARQL_QUERY2 where {<%s> ?p ?o FILTER (REGEX(STR(?p), 'http://pt.dbpedia.org/resource/Compilador'))}"

  def getContent(endpoint: String, urlParameters: String): String = {

    val url: URL = new URL(endpoint)
    val connection: HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.setRequestProperty("Accept", "application/json")
    connection.setRequestProperty("Request-Method", "POST")
    connection.setRequestProperty("Accept-Charset", "utf-8")
    connection.setRequestProperty("Connection", "keep-alive")
    connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length))
    connection.setRequestMethod("POST")
    connection.setInstanceFollowRedirects(false)
    connection.setUseCaches(false)

    connection.setDoOutput(true)
    connection.setDoInput(true)
    connection.connect()

    val bufferWriter: OutputStreamWriter = new OutputStreamWriter(connection.getOutputStream)
    bufferWriter.write(urlParameters)
    bufferWriter.flush()

    val bufferReader: BufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream))

    val buffer: StringBuilder = new StringBuilder
    var line: String = ""

    while (bufferReader.ready()) {
      line = bufferReader.readLine()
      if (line == null) break()
      buffer.append(line)
    }

    bufferReader.close()
    bufferWriter.close()

    buffer.toString()

  }

  def getUrlParameters(text: String, encoding: String="utf-8"): String = {

    val parameters: Properties = new Properties()

    parameters.setProperty("query", URLEncoder.encode(text, encoding))
    parameters.setProperty("default-graph-uri", URLEncoder.encode("http://dbpedia.org", encoding))
    parameters.setProperty("timeout", "30000")
    parameters.setProperty("debug", "on")

    val iterator: util.Iterator[AnyRef] = parameters.keySet().iterator()

    var first: Int = 0

    val buffer: StringBuilder = new StringBuilder

    while (iterator.hasNext) {
      val name: String = iterator.next().asInstanceOf[String]
      val value: String = parameters.getProperty(name)

      if (first != 0)
        buffer.append("&")

      buffer.append(name)
      buffer.append("=")
      buffer.append(value)

      first += 1

    }

    buffer.toString()

  }

}
