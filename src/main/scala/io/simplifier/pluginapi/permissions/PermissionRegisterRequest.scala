package io.simplifier.pluginapi.permissions

import org.json4s.JObject

/**
 * Request from the plugin to the appSever to register a PermissionObject.
 * @author Christian Simon
 * 
 * @param name name of the permission object
 * @param possibleCharacteristics characteristics to use in the admin ui 
 */
case class PermissionRegisterRequest(technicalName: String, name: String, description: String, possibleCharacteristics: JObject)