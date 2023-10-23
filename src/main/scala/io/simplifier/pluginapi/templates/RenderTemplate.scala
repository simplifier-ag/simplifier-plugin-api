package io.simplifier.pluginapi.templates

import org.json4s._
import io.simplifier.pluginapi.resultMapper
import io.simplifier.pluginapi.rest.PluginApiMessage

object RenderTemplate extends resultMapper {

  abstract class request extends PluginApiMessage
  case class renderTemplate(template:String,params:JValue) extends request
  case class renderFile(name:String, namespace:String,params:JValue) extends request

  abstract class result
  case class success(success:Boolean,renderResult:String) extends result {
    def this(renderResult:String) = this(true,renderResult)
  }
  case class error(success:Boolean,msg:String) extends result {
    def this(msg:String) = this(false,msg)
  }

  type RESULT = result
  type SUCCESS = success
  type ERROR = error
  protected val successTrigger="success"

}
