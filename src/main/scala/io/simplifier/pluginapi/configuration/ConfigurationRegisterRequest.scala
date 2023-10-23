package io.simplifier.pluginapi.configuration

/**
 * Request from the plugin to the appSever to register a PermissionObject.
 * @author Christian Simon
 * 
 * @param pluginName name of the plugin
 * @param configLabel label in configuration UI
 * @param configView OpenUI5 view URL for configuration UI
 */
case class ConfigurationRegisterRequest(pluginName: String, configLabel: String, configView: String)