package io.simplifier.pluginapi.mail

import org.json4s._
import io.simplifier.pluginapi.resultMapper
import io.simplifier.pluginapi.rest.PluginApiMessage

/**
  * send emails messages
  */
object SendEmails extends resultMapper {

  abstract class request extends PluginApiMessage
  sealed abstract class result

  case class binContent(mime:String, filename:String, content: Option[Array[Byte]], uploadSession: Option[String], contentB64: Option[String])
  case class content(mime:String, charset:String, content:String)
  case class email(receiver:String, subject:String, cc:List[String], bcc: List[String], body:content, emailUUID:String,
                   sender: Option[String] = None)
  case class bulkEmails(emails:List[email], attachments:List[binContent] ) extends request

  case class emailTransport(success:Boolean,errorMessage:Option[String])
  {
    def this() = this(true,None)
    def this(msg:String) = this (false,Some(msg))
  }
  case class success(success:Boolean,transports:Map[String,emailTransport]) extends result {
    def this(tr:Map[String,emailTransport]) = this(true,tr)
  }
  case class error(success:Boolean,message:String) extends result {
    def this(msg:String) = this(false,msg)
  }

  def resultFromJson(json:JValue):Option[result] = {
    implicit val formats: Formats = DefaultFormats
    try {
      if ((json \ "success").extract[Boolean])
        Some(json.extract[success])
      else
        Some(json.extract[error])
    }
    catch {
      case e:Exception => None
    }
  }

  def resultFromJson(optjson:Option[JValue]):Option[result] = optjson flatMap (json => resultFromJson(json))

  type RESULT = result
  type SUCCESS = success
  type ERROR = error
  protected val successTrigger="success"

}
