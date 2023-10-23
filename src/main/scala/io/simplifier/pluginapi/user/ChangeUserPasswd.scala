package io.simplifier.pluginapi.user

import io.simplifier.pluginapi.rest.PluginApiMessage


object ChangeUserPasswd {

  abstract class request extends PluginApiMessage

  case class byUserId(userId:Long, cleartextPasswd:String) extends request

  case class byUserLoginName(userLogin:String, cleartextPasswd:String) extends request

  case class currentUser(cleartextPasswd:String) extends request

  case class byPasswordEmail(templateNamespace: Option[String],
                            templateName: Option[String],
                            resetURI: String,
                            subject: Option[String],
                            emailMime: String = "text/html",
                            emailCharset: String = "UTF-8",
                            additionalData: Option[Map[String, String]],
                            language: Option[String],
                            userId: Long) extends request

  case class byPasswordEmails(emails: Seq[byPasswordEmail]) extends request

  case class result(success:Boolean,ErrorMessage:Option[String]) {
    def this()=this(true,None)
    def this(errorMessage:String)=this(false,Some(errorMessage))
  }

}
