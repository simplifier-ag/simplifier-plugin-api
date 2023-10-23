package io.simplifier.pluginapi.user

import io.simplifier.pluginapi.rest.PluginApiMessage

object CreateUser {
  abstract class request extends PluginApiMessage
  case class userInfo(loginName: String,
                      firstName: String,
                      lastName: String,
                      email: String,
                      mobileNumber: Option[String],
                      activeFrom: Option[String],
                      activeTill: Option[String],
                      active: Option[Boolean],
                      salutation: Option[String],
                      roles: Option[Seq[String]] = None,
                      isBlocked: Option[Boolean] = None
                     ) extends request

  case class userInfos(infos: Seq[userInfo]) extends request

  case class userInfoWithNotification(loginName: String,
                                      firstName: String,
                                      lastName: String,
                                      email: String,
                                      mobileNumber: Option[String],
                                      activeFrom: Option[String],
                                      activeTill: Option[String],
                                      active: Option[Boolean],
                                      salutation: Option[String],
                                      serverURL: String,
                                      language: Option[String],
                                      roles: Option[Seq[String]] = None,
                                      templateName: Option[String] = None,
                                      templateNamespace: Option[String] = None,
                                      parameters: Option[Map[String, String]] = None,
                                      isBlocked: Option[Boolean] = None
                                     ) extends request

  abstract class result
  case class userId(success: Boolean, id: Long, login: String) extends result
  case class userIds(results: Seq[userId], errors: Seq[error], success: Boolean) extends result
  case class error(success: Boolean, msg:String) extends result
}
