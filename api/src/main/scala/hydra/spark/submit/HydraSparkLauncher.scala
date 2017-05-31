package hydra.spark.submit

import java.util.UUID

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import hydra.spark.api.DispatchDetails
import hydra.spark.internal.Logging
import org.apache.spark.launcher.SparkLauncher

import scala.collection.JavaConverters._

/**
  * Created by alexsilva on 1/18/17.
  */
object HydraSparkLauncher extends Logging {

  private[submit] def env[T](dispatchDetails: DispatchDetails[T], sparkInfo: SparkSubmitInfo): Map[String, String] = {
    import hydra.spark.configs._
    val env: Map[String, String] = Map(
      "HADOOP_CONF_DIR" -> sparkInfo.hadoopConfDir,
      "YARN_CONF_DIR" -> sparkInfo.yarnConfDir
    ).collect {
      case (key, Some(value)) => key -> value
    }

    env ++ dispatchDetails.dsl.flattenAtKey("env").map { case (k, v) => k.substring(4) -> v }

  }

  def createLauncher[T](sparkInfo: SparkSubmitInfo, dispatch: DispatchDetails[T]): SparkLauncher = {
    import configs.syntax._

    val dsl = dispatch.dsl.root().render(ConfigRenderOptions.concise())
    val launcher = new SparkLauncher(env(dispatch, sparkInfo).asJava)
      .setSparkHome(sparkInfo.sparkHome)
      .setAppResource(sparkInfo.hydraSparkJar)
      .setAppName(dispatch.dsl.get[String]("name").valueOrElse(UUID.randomUUID().toString))
      .setMainClass("hydra.spark.dsl.DslRunner")
      .addAppArgs(dsl)
      .setMaster(dispatch.sparkConf.get("spark.master"))
      .setVerbose(true)

    dispatch.sparkConf.getAll.foreach(entry => launcher.setConf(entry._1, entry._2))

    launcher
  }


  val defaultSparkCfg = ConfigFactory.parseMap(
    Map(
      "spark.ui.enabled" -> "false",
      "spark.yarn.queue" -> "hydra.dispatch",
      "spark.yarn.am.memory" -> "1g",
      "spark.driver.memory" -> "1g",
      "spark.akka.frameSize" -> "200",
      "spark.executor.memory" -> "8g",
      "spark.executor.instances" -> "8",
      "spark.executor.cores" -> "12",
      "spark.default.parallelism" -> "5000"
    ).asJava
  )

}


case class SparkSubmitInfo(sparkHome: String, hydraSparkJar: String, hadoopConfDir: Option[String],
                           yarnConfDir: Option[String])
