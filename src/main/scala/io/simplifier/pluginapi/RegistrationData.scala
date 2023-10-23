package io.simplifier.pluginapi

import io.simplifier.pluginapi.rest.PluginApiMessage

/**
  * Plugin registration data transmitted for registration of a new plugin to the Simplifier AppServer.
  */
object RegistrationData {

  /**
    * Plugin manifest, describing the plugin details and capabilities.
    * @param plugin plugin details
    * @param connection information for the appserver on how to communicate with the plugin
    * @param proxyInterface proxy interface capability (optional)
    * @param slotInterface slot interface capability
    * @param configurationInterface configuration interface capability
    * @param permissionObjects permission object declared by the plugin (optional)
    */
  case class PluginManifest(
                             plugin: PluginDetails,
                             connection: PluginHttpConnection,
                             proxyInterface: Option[PluginProxyInterface],
                             slotInterface: Option[PluginSlotInterface],
                             configurationInterface: Option[PluginConfigurationInterface],
                             permissionObjects: Option[Seq[PermissionObjectDefinition]]
                           ) extends PluginApiMessage

  case class PermissionObjects(
                                permissionObjects: Option[Seq[PermissionObjectDefinition]]
                              ) extends PluginApiMessage

  /**
    * Plugin details
    * @param name name of the plugin
    * @param description description of the plugin (show in plugin overview)
    * @param vendor plugin vendor
    * @param version plugin version
    * @param documentationUrl optional URL where the documentation of the plugin is available
    */
  case class PluginDetails(name: String, description: String, vendor: String, version: String, documentationUrl: Option[String])

  /**
    * Connection settings.
    * @param host hostname of the plugin, from the app server's view
    * @param port port of the plugin, from the app server's view
    */
  case class PluginHttpConnection(host: String, port: Int)

  /**
    * Proxy interface capabilities
    * @param baseUrl url of the proxy interface root, relative from the plugin http service's root
    * @param authenticationRequired flag, if only authenticated requests are allowed to be transferred to this interface
    */
  case class PluginProxyInterface(baseUrl: String, authenticationRequired: Boolean)

  /**
    * Slot interface capabilities
    * @param baseUrl url of the slot interface root, relative from the plugin http service's root
    * @param slots   defined list of slots
    * @param standaloneOnlySlots set of names of Slots, which are only allowed to be executed in
    *                            Standalone (=No Cluster) Mode or on the Primary Cluster server
    */
  case class PluginSlotInterface(baseUrl: String, slots: Seq[String], standaloneOnlySlots: Option[Set[String]])

  /**
    * Configuration view.
    * @param title title of the view in the admin UI
    * @param viewUri URI pointing to the path on where the configuration interface will provide the OpenUI5 JSON view
    */
  case class PluginConfigurationView(title: String, viewUri: String)

  /**
    * Configuration interface capabilities
    * @param baseUrl url of the configuration interface root, relative from the plugin http service's root
    * @param views views defined for plugin configuration in the admin UI
    */
  case class PluginConfigurationInterface(baseUrl: String, views: Seq[PluginConfigurationView])

}