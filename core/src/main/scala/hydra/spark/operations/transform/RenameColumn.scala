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

package hydra.spark.operations.transform

import hydra.spark.api.{ DFOperation, ValidationResult }
import org.apache.spark.sql.DataFrame

import scala.util.Try

/**
 * Created by alexsilva on 8/16/16.
 */
case class RenameColumn(oldName: String, newName: String) extends DFOperation {
  override def id: String = s"rename-column-$newName"

  override def transform(df: DataFrame): DataFrame = {
    Try(df(oldName)).map { col =>
      df.withColumnRenamed(oldName, newName)
    }.getOrElse(df)

  }

  override def validate: ValidationResult = {
    checkRequiredParams(Seq(("newName", newName), ("oldName", oldName)))
  }
}
