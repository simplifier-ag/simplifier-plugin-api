package io.simplifier.pluginapi.serverSettings

import io.simplifier.pluginapi.rest.PluginApiMessage

/**
 * Server settings slot messages
 */
object ReadServerSettings {

  case class Settings(activatePluginPermissionCheck: Boolean) extends PluginApiMessage


  /**
   * an empty case class, because we need a body for the POST request
   */
  case class ServerSettingsRequest() extends PluginApiMessage
}
