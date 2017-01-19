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

package hydra.spark.sources

import hydra.spark.api.Source
import hydra.spark.util.RDDConversions._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

/**
 * Just a utility class so that row base sources don't have to implement the toDF function.
 *
 * Created by alexsilva on 1/3/17.
 */
abstract class RowSource extends Source[Row] {

  override def toDF(rdd: RDD[Row]) = rdd.toDF

}
