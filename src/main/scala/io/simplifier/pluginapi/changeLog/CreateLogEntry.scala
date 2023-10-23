package io.simplifier.pluginapi.changeLog

import io.simplifier.pluginapi.changeLog.Actions._
import io.simplifier.pluginapi.changeLog.Sources._
import io.simplifier.pluginapi.rest.PluginApiMessage
import org.json4s._

/**
  * Create log entry messages
  */
object CreateLogEntry {

  implicit val formats: Formats = DefaultFormats
  abstract class request

  case class entry(source:Sources,action:Actions,before:Option[JValue],after:Option[JValue],
                   description:Option[String],technicalSource:Option[String],
                   dbModelInfo:Option[JValue],dbModelId:Option[String],
                   triggeredFrom:String ) extends request with PluginApiMessage {

    def toJson = JObject(for (i <- List(
      Some(JField("source", JString(source.toString)) ),
      Some(JField("action", JString(action.toString)) ),
      before.map(j => JField("before",j)),
      after.map(j => JField("after",j)),
      description.map(s => JField("description",JString(s)) ),
      technicalSource.map(s => JField("technicalSource",JString(s)) ),
      dbModelInfo.map(j => JField("dbModelInfo",j) ),
      dbModelId.map(s => JField("dbModelId",JString(s)) ),
      Some(JField("triggeredFrom", JString(triggeredFrom)) )
      ) if i.isDefined) yield i.get
    )

    def this(j:JValue)(implicit formats: Formats)=this(
      Sources.withName((j \ "source" \ "name").extract[String]),
      Actions.withName((j \ "action" \ "name").extract[String]),
      j \ "before" match {
        case JNothing => None
        case v:JValue  => Some(v)
      },
      j \ "after" match {
        case JNothing => None
        case v:JValue  => Some(v)
      },
      j \ "description" match {
        case JNothing => None
        case JString(s)  => Some(s)
        case _ => None
      },
      j \ "technicalSource" match {
        case JNothing => None
        case JString(s)  => Some(s)
        case _ => None
      },
      j \ "dbModelInfo" match {
        case JNothing => None
        case v:JValue  => Some(v)
      },
      j \ "dbModelId" match {
        case JNothing => None
        case JString(s)  => Some(s)
        case _ => None
      },
      (j \ "triggeredFrom").extract[String]
    )

  }

  abstract class result

  case class success(success: Boolean, entryId:Long) extends result {
    def this(entryId:Long) = this(true,entryId)
  }

  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }
}
