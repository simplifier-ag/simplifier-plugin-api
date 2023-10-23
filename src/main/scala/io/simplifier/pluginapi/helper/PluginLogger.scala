package io.simplifier.pluginapi.helper

import org.slf4j.{Logger, LoggerFactory}

/**
 * Logger trait for Plugins.
 * @author Christian Simon
 */
trait PluginLogger {

  /**
   * Slf4j Logger.
   */
  lazy val log: Logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

}

object PluginLogger {

  /*
   * Note: Since there is no shared code between the Util module and the pluginApi module,
   * this is a functional copy of the Code in com.itizzimo.appServer.util.logging.Logging in Util.
   */

  private val ENV_CLUSTER_MEMBER_NAME = "CLUSTER_MEMBER_NAME"
  private val LOG_PROPERTY_LOGFILE_BASENAME = "LOGFILE_BASENAME"

  /**
    * Initializes the logging variable "LOGFILE_BASENAME" for the base filename of the logfile,
    * which should give the default "myPlugin" when Cluster Mode is disabled, and "myPlugin-${ClusterMemberName}"
    * when a cluster member is defined via Environment Variable (or System Property).<br/>
    * <b>This function should be called before the very first log output, so even before settings.conf parsing.</b>
    *
    * @param defaultBaseName Normal base name of the logfile (e.g, "myPlugin" for "myPlugin.log")
    */
  def initializeClusterModeLoggingProperties(defaultBaseName: String): Unit = {
    val clusterMemberNameOpt =
      Option(System.getProperty(ENV_CLUSTER_MEMBER_NAME)).map(reduceToTechnicalName).filter(_.nonEmpty) orElse
        Option(System.getenv(ENV_CLUSTER_MEMBER_NAME)).map(reduceToTechnicalName).filter(_.nonEmpty)
    val logfileBasename = clusterMemberNameOpt match {
      case Some(clusterMemberName) => s"$defaultBaseName-$clusterMemberName"
      case None => defaultBaseName
    }
    System.setProperty(LOG_PROPERTY_LOGFILE_BASENAME, logfileBasename)
  }

  private def reduceToTechnicalName(name: String): String = name.replaceAll("[^a-zA-Z0-9_]+", "")

}