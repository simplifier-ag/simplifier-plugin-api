package io.simplifier.pluginapi.role

/**
  * UnassignRole slot messages
  */
object UnassignRole {

  abstract class request
  case class UserToRole(userId:Long, roleId:String) extends request

  abstract class result
  case class success(success: Boolean) extends result
  {
    def this() = this(true)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

}
