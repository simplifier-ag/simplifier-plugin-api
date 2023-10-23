package io.simplifier.pluginapi.slots

import java.nio.charset.StandardCharsets.UTF_8

import io.simplifier.pluginapi.{HttpPostResponse, JSON}
import io.simplifier.pluginapi.UserSession
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * General trait for Plugin Slats that handle JSON and need implicit [[DefaultFormats]].
  */
trait JsonFormats {

  implicit val formats: Formats = DefaultFormats

}

/**
  * Abstract trait defining an operation which can be executed as [[HttpJsonSlot]] or [[JsonSlot]].
  */
trait JsonOperation extends JsonFormats {

  /**
    * Execute slot and create result.
    * @param item parameter as JSON value
    * @param userSession user session
    * @return result JSON value
    */
  def result(item: JValue)(implicit userSession: UserSession): JValue

}

/**
 * Abstract HTTP slot for JSON processing.
 * @author Christian Simon
 */
trait HttpJsonSlot extends ChunkedHttpSlot with JsonFormats {

  self: JsonOperation =>

  val contentType = "application/json"

  override def slot(item: JValue)(implicit userSession: UserSession): HttpPostResponse = {
    HttpPostResponse(contentType, compact(render(result(item))).getBytes(UTF_8))
  }

}

/**
 * Abstract slot for JSON processing.
 * @author Christian Simon
 */
trait JsonSlot extends ChunkedSlot {

  self: JsonOperation =>

  override def slot(item: JValue)(implicit userSession: UserSession): Unit = {
    sender !! JSON(result(item), userSession)
  }

}
