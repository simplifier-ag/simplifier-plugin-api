package io.simplifier.pluginapi.user

object DeleteUser {
  abstract class request
  case class byLogin(userLogin: String) extends request
  case class byId(userId: Long) extends request

  abstract class result
  case class success(success: Boolean) extends result
  {
    def this() = this(true)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }
}
