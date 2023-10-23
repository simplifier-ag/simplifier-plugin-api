package io.simplifier.pluginapi


package object changeLog {

  import org.json4s._

  /**
    * enum for change log sources
    */
  object Sources extends Enumeration {
    val Plugin = Value("Plugin")
    val AppServer = Value("AppServer")
    val App = Value ("App")
    type Sources = Value
  }

  /**
    * enum for change log actions
    */
  object Actions extends Enumeration {
    val Create = Value("Create")
    val Update = Value("Update")
    val Delete = Value ("Delete")
    val Read = Value("Read")
    val Invalidate = Value("Invalidate")
    val Validate = Value("Validate")
    type Actions = Value
  }

  /**
    * enum for filter action
    */
  object Filters extends Enumeration {
    val bySource = Value("bySource")
    val byAction = Value("byAction")
    val byTechnicalSource = Value("byTechnicalSource")
    val byUserLogin = Value("byUserLogin")
    val byUserEmail = Value("byUserEmail")
    val byTrigger = Value("byTrigger")
    val Undefined = Value("N/A")

    type Filters = Value

    def fingerprint(param:JValue) : Filters = {
      implicit val formats = DefaultFormats
      if ((param \ "source").isInstanceOf[JString])
        return bySource
      if ((param \ "action").isInstanceOf[JString])
        return byAction
      if ((param \ "technicalSource").isInstanceOf[JString])
        return byTechnicalSource
      if ((param \ "userLogin").isInstanceOf[JString])
        return byUserLogin
      if ((param \ "userEmail").isInstanceOf[JString])
        return byUserEmail
      if ((param \ "triggeredFrom").isInstanceOf[JString])
        return byTrigger
      Undefined
    }

  }
}

