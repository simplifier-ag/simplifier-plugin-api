package io.simplifier.pluginapi.changeLog

import java.sql.Timestamp

import org.json4s._

/**
  * read log entry messages
  */
object ReadLogEntry {


  case class changeLogEntry(userId:Long,source:String,action:String,before:Option[JValue],after:Option[JValue],
                            description:Option[String],technicalSource:Option[String],
                            userLogin:String, userFirstName:String, userLastName:String,
                            userEmail:String, dbModelInfo:Option[JValue],dbModelId:Option[String],
                            triggeredFrom:String, changeDate:Timestamp)

  abstract class request
  case class byId(entryId:Long) extends request

  abstract class result
  case class success(success: Boolean, item:changeLogEntry) extends result {
    def this(i:changeLogEntry) = this(true,i)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

}
