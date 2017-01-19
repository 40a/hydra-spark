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

package hydra.spark.operations.hadoop

import java.io.File
import java.nio.file.Files

import hydra.spark.testutils.{ SharedSparkContext, StaticJsonSource }
import org.apache.commons.io.FileUtils
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{ SparkConf, SparkContext }
import org.scalatest.{ BeforeAndAfterAll, FunSpecLike, Matchers }
import spray.json._

/**
 * Created by alexsilva on 1/4/17.
 */
class HiveTableSpec extends Matchers with FunSpecLike with BeforeAndAfterAll with DefaultJsonProtocol
    with SharedSparkContext {

  val warehouseDir = makeWarehouseDir()

  val sparkConf = new SparkConf()
    .setMaster("local")
    .setAppName("hydra")
    .set("spark.ui.enabled", "false")
    .set("spark.local.dir", "/tmp")
    .set("spark.driver.allowMultipleContexts", "true")
    .set("spark.sql.test", "")
    .set("spark.sql.warehouse.dir", warehouseDir.toURI.getPath)
    .set(HiveConf.ConfVars.METASTOREWAREHOUSE.varname, warehouseDir.toURI.getPath)

  // var sparkContext = new SparkContext(sparkConf)

  override def afterAll(): Unit = {
    super.afterAll()
    warehouseDir.delete()
    FileUtils.deleteDirectory(new File("metastore_db"))
    //  sparkContext.stop()
  }

  describe("When writing to Hive") {
    it("should save") {
      val hive = new HiveContext(sc)
      val df = StaticJsonSource.createDF(hive)
      HiveTable("test", Map("option.path" -> warehouseDir.toURI.getPath), Seq.empty).transform(df)
      val dfHive = hive.sql("SELECT * from test")

      val hiveDf = dfHive.toJSON.collect()
        .map(_.parseJson.asJsObject.fields.filter(!_._1.startsWith("data"))).map(new JsObject(_))
      val datelessDf = df.toJSON.collect()
        .map(_.parseJson.asJsObject.fields.filter(!_._1.startsWith("data"))).map(new JsObject(_))

      datelessDf.foreach { json =>
        hiveDf should contain(json)
      }
    }
  }

  def makeWarehouseDir(): File = {
    val warehouseDir = Files.createTempDirectory("_hydra").toFile
    warehouseDir
  }
}
