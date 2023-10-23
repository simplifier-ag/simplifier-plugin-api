package io.simplifier.pluginapi.user

import io.simplifier.pluginapi.rest.PluginApiMessage


object UpdateUser {

  case class UserInfo(var loginName: String,
                      var firstName: String,
                      var lastName: String,
                      var email: String,
                      var mobileNumber: Option[String],
                      var activeFrom: Option[String],
                      var activeTill: Option[String],
                      var active: Option[Boolean],
                      var salutation: Option[String]) {
    def this(i : ReadUser.userInfo) = this (
      i.loginName, i.firstName, i.lastName, i.email,
      i.mobileNumber, i.activeFrom, i.activeTill,
      i.active, i.salutation
    )
  }
  abstract class request extends PluginApiMessage
  case class byId(userId:Long, userInfo: Option[UserInfo]) extends request
  case class byIds(user: Seq[UpdateUser.byId]) extends request
  case class byLogin(userLogin:String, userInfo:Option[UserInfo]) extends request
  case class byLogins(userLogins:Seq[UpdateUser.byLogin]) extends request
  case class actUser(userInfo:UserInfo) extends request

  abstract class result

  case class successes(updatedUsers: Seq[success], errors: Seq[error], success: Boolean) extends result

  case class success(login: String, success: Boolean) extends result
  {
    def this(login: String) = this(login, true)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

}
