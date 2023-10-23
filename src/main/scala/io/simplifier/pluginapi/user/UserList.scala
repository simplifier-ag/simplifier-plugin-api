package io.simplifier.pluginapi.user

object UserList {
  case class listItem(userId:Long, userLogin:String)

  abstract class result
  case class success(success: Boolean, userList: List[listItem]) extends result
  {
    def this(userList: List[listItem]) = this(true,userList)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }
}
