package io.simplifier.pluginapi.role

import io.simplifier.pluginapi.rest.PluginApiMessage

/**
  * Role List slot messages
  */
object RoleListWithPermissions {
  case class RoleWithPermissions(name: String, permissions: Seq[MinimalPermissionInfo])
  case class MinimalPermissionInfo(permissionName: String, characteristicsWithValues: Seq[CharacteristicWithValues])
  case class CharacteristicWithValues(characteristic: String, values: Set[String])
  abstract class result
  case class success(success: Boolean, roleList: List[RoleWithPermissions]) extends result with PluginApiMessage

  object success
  {
    def apply(roleList: List[RoleWithPermissions]): success = success(success = true, roleList)
  }
  case class error(success: Boolean, msg:String) extends result

  object error {
    def apply(msg:String): error = error(success = false, msg)
  }
}
