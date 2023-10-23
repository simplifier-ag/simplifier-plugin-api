package io.simplifier.pluginapi.permissions

/**
 * Request to query the appServer for a specific permission.
 * @author Christian Simon
 */
case class PermissionQueryRequest(permissionObjectName: String)