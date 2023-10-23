package io.simplifier.pluginapi.slots

import akka.actor._
import io.simplifier.pluginapi._
import io.simplifier.pluginapi.helper.PluginLogger
import io.simplifier.pluginapi.UserSession
import org.json4s._

/**
 * Synchronous actor used for http requests.
 */
trait HttpSlot extends Actor with PluginLogger {

  /**
   * Abstract def for slot
   * @param param slot parameter
   * @param userSession user session
   * @return httpPostResponse
   */
  def slot(param: JValue)(implicit userSession: UserSession): HttpPostResponse

  def receive: Receive = {
    case HttpPost(item, userSession) =>
      sender ! slot(item)(userSession)
    case JSON(item, userSession) =>
      sender ! slot(item)(userSession)
    case other  =>
      log.error(s"Received unexpected actor message in ${getClass.getName}: $other")
  }

}
