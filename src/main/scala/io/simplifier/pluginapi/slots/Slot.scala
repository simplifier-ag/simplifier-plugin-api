package io.simplifier.pluginapi.slots

import akka.actor._
import io.simplifier.pluginapi._
import io.simplifier.pluginapi.helper.PluginLogger
import io.simplifier.pluginapi.UserSession
import org.json4s._

/**
  * Synchronous or asynchronous actor handling Slot Messages.
  */
trait Slot extends Actor with PluginLogger {

  /**
    * Abstract def for slot. If slot wants to answer the request, it must use tell by itself.
    * @param param slot parameter
    * @param userSession user session
    */
  def slot(param: JValue)(implicit userSession: UserSession): Unit

  def receive: Receive = {
    case JSON(item, userSession) =>
      slot(item)(userSession)
    case other =>
      log.error(s"Received unexpected actor message in ${getClass.getName}: $other")
  }

}
