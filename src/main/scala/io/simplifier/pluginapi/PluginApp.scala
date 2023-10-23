package io.simplifier.pluginapi

import io.simplifier.pluginapi.helper.PluginLogger

/**
 * Abstract App Superclass for all Plugins.
 * @author Christian Simon
 */
abstract class PluginApp(logfileBaseName: String) extends App with PluginLogger {

  /*
   * Plugin implementation
   */

  val globalSettings: PluginGlobalSettings
  val globals: PluginGlobals
  val baseSlot: BaseProps

  /*
   * Startup / Shutdown
   */

  /**
   * Abstract def for custom init code.
   */
  def init(): Unit = {}

  /**
   * Abstract def for custom shutdown.
   */
  def shutdown(): Unit = {}

  /**
   * Plugin Startup.
   */
  def startUpPlugin(): Unit = {
    log.info("Startup Plugin ...")
    globalSettings.initSettingsFromCommandline(args)
    globals.init(baseSlot)
    sys.addShutdownHook(pluginShutdown)
    init()
  }

  /**
   * Plugin Shutdown.
   */
  def pluginShutdown(): Unit = {
    log.info("Shutdown Plugin ...")
    shutdown()
    globals.shutdown
  }

  /**
    * Run application (overwrite for admin calls)
    */
  def runApp(): Unit = {
    startUpPlugin()
  }

  // This sets the logfile basename, so it must be called before the first Logger is initialized!
  PluginLogger.initializeClusterModeLoggingProperties(logfileBaseName)

  // Run plugin ...
  runApp()

}