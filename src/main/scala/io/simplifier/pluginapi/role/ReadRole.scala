package io.simplifier.pluginapi.role

import io.simplifier.pluginapi.rest.PluginApiMessage

/**
  * read Role slot messages
  */
object ReadRole {

  case class PermissionInfo(technicalName: String, name: String, description: String, characteristicTechnicalName: String,
                            characteristicName: String, characteristicDescription: String, characteristicValue: Set[String],
                            characteristicDisplayType : String)

  abstract class request extends PluginApiMessage
  case class byId(roleId:String) extends request
  case class byName(roleName:String) extends request

  abstract class result
  case class success(success: Boolean, id:String, name: String, description: String, active: Boolean,
                     permissions:List[PermissionInfo]) extends result
  case class error(success: Boolean, msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

}
