/*
 * Copyright (C) 2017 Pluralsight, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hydra.spark.operations.io

import java.io.File

import hydra.spark.api._
import hydra.spark.avro.SchemaRegistrySupport
import hydra.spark.internal.Logging
import org.apache.avro.Schema
import org.apache.spark.sql.DataFrame

import scala.util.Try

/**
  * Created by alexsilva on 6/21/16.
  */
case class SaveAsParquet(directory: String, schema: String, codec: Option[String], properties: Map[String, String])
  extends DFOperation with SchemaRegistrySupport with Logging {

  override val id: String = s"save-as-parquet-$directory-$codec"

  private lazy val avroSchema: Schema = getValueSchema(schema)

  override def transform(df: DataFrame): DataFrame = {
    val path = new File(directory, System.currentTimeMillis.toString)
    df.write.parquet(path.getAbsolutePath)
    df
  }


  override def validate: ValidationResult = {
    Try {
      checkRequiredParams(Seq(("schema", schema)))
      //force parsing
      log.debug(s"Using schema: ${avroSchema.getFullName}")
      Valid
    }.recover { case t => Invalid("avro", t.getMessage) }.get
  }
}


