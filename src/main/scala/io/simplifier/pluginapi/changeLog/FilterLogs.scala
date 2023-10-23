package io.simplifier.pluginapi.changeLog

/**
  * Filter log entry messages
  */
object FilterLogs {

  case class Page(start:Int, length:Int)

  abstract class request
  case class bySource(source:String,pagination:Option[Page]) extends request
  case class byAction(action:String,pagination:Option[Page]) extends request
  case class byTechnicalSource(technicalSource:String,pagination:Option[Page]) extends request
  case class byUserLogin(userLogin:String,pagination:Option[Page]) extends request
  case class byUserEmail(userEmail:String,pagination:Option[Page]) extends request
  case class byTrigger(triggeredFrom:String,pagination:Option[Page]) extends request


  case class changeLogListEntry(id:Long,userId:Long,source:String,action:String,technicalSource:Option[String])

  abstract class result
  case class success(success: Boolean, list:List[changeLogListEntry]) extends result {
    def this(lst:List[changeLogListEntry]) = this(true,lst)
  }
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

}
