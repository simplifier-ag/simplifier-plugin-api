package io.simplifier.pluginapi.role

/**
  * Role List slot messages
  */
object RoleList {
  case class listItem(id:String, name: String, description: String, active: Boolean)

  abstract class result
  case class success(success: Boolean, roleList: List[listItem]) extends result

  object success
  {
    def apply(roleList: List[listItem]): success = success(success = true, roleList)
  }
  case class error(success: Boolean, msg:String) extends result

  object error {
    def apply(msg:String): error = error(success = false, msg)
  }
}
