package io.simplifier.pluginapi.permissions

/**
 * Single permission object for any role of the queried user.
 * If the user had multiple roles which were assigned to the permission object,
 * one instance of this class will be returned for each instance of the permission object
 * (so different characteristics for the same permission object are supported).
 *
 * @param permissionObjectName name of the permission object
 * @param characteristics map containing the characteristics of the permission object
 */
case class PermissionObjectResult(permissionObjectName: String, characteristics: Map[String, Set[String]])

/**
 * Result class from permission object check.
 * @author simon
 */
case class PermissionQueryResult(permissions: Seq[PermissionObjectResult]) {

  /**
   * Check if for existence of permission object.
   * @param permissionObjectName of permission object
   * @return true if the permission object is contained (with any characteristic)
   */
  def hasPermission(permissionObjectName: String): Boolean =
    permissions.exists(_.permissionObjectName == permissionObjectName)

  /**
   * Check if for existence of permission object with specific boolean characteristic.
   * @param permissionObjectName of permission object
   * @param characteristicName characteristic name to check
   * @return true if the permission object is contained (with the given characteristic as "true")
   */
  def hasPermission(permissionObjectName: String, characteristicName: String): Boolean =
    hasPermission(permissionObjectName, characteristicName, "true")

  /**
   * Check if for existence of permission object with specific characteristic.
   * @param permissionObjectName of permission object
   * @param characteristicName characteristic name to check
   * @param characteristicValue characteristic value to check
   * @return true if the permission object is contained (with the given characteristic)
   */
  def hasPermission(permissionObjectName: String, characteristicName: String,
                    characteristicValue: String): Boolean =
    permissions.exists {
      p =>
        p.permissionObjectName == permissionObjectName && p.characteristics.get(characteristicName).contains(characteristicValue)
    }

}