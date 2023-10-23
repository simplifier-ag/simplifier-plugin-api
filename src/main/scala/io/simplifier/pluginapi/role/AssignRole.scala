package io.simplifier.pluginapi.role

import io.simplifier.pluginapi.rest.PluginApiMessage

/**
  * Assign role slot messages
  */
object AssignRole {

  abstract class request extends PluginApiMessage
  case class UserToRole(userId:Long, roleId:String) extends request
  case class UsersToRole(usersToRole: Seq[UserToRole]) extends request

  abstract class result
  case class success(success: Boolean) extends result
  {
    def this() = this(true)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

}
