package io.simplifier.pluginapi.user

import io.simplifier.pluginapi.rest.PluginApiMessage

import java.time.LocalDateTime


object ReadUser {
  case class PermissionInfo(technicalName: String, name: String, description: String, characteristicTechnicalName: String,
                            characteristicName: String, characteristicDescription: String, characteristicValue: Set[String], characteristicDisplayType : String)
  case class RoleInfo(name: String, description: String, active: Boolean, permissions: List[PermissionInfo])
  case class GroupInfo(name: String, description: String)

  abstract class request extends PluginApiMessage
  case class byId(userId:Long) extends request
  case class byLogin(userLogin:String) extends request
  case class byLogins(users: Seq[byLogin]) extends request


  abstract class result
  case class userInfo(success: Boolean,
                      id:Long,
                      groups: List[GroupInfo],
                      roles: List[RoleInfo],
                      loginName: String,
                      firstName: String,
                      lastName: String,
                      email: String,
                      serverURL: Option[String],
                      language : Option[String],
                      mobileNumber: Option[String],
                      activeFrom: Option[String],
                      activeTill: Option[String],
                      active: Option[Boolean],
                      salutation: Option[String],
                      createdOn: Option[LocalDateTime],
                      lastLogin: Option[LocalDateTime]) extends result

  case class userInfos(userInfos: Seq[userInfo], errors: Seq[error], success: Boolean) extends result

  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }
}