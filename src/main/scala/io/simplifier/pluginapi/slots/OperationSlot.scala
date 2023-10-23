package io.simplifier.pluginapi.slots

import io.simplifier.pluginapi.{ChunkHelper, PluginGlobals}
import io.simplifier.pluginapi.helper.PluginLogger
import io.simplifier.pluginapi.UserSession
import org.json4s.Extraction.{decompose, extract}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

/**
  * Companion Object to [[OperationSlot]].
  */
object OperationSlot {

  /** Rest message for JSON output */
  case class RestMessage(msgText: String, msgId: String)

  /** Exception leading to a REST error message. */
  case class RestMessageException(msgText: String) extends Exception(msgText)

  /** Exception containing a predefined REST error message */
  case class OperationException(message: RestMessage) extends Exception(message.msgText)

  object OperationException {
    def apply(msgText: String, msgId: String): OperationException = apply(RestMessage(msgText, msgId))
  }

  /** REST response object containing an error */
  case class OperationError(error: String, errorCode: Option[String], success: Boolean = false)

  /** REST response object containing an error */
  case class OperationSuccess(result: Option[Any], success: Boolean = true)

  object OperationError {
    def apply(msg: RestMessage): OperationError = apply(msg.msgText, Option(msg.msgId).filter(_.nonEmpty))
  }

}

/**
  * Abstract Slot definition for a Plugin Operation, which can be implemented as [[Slot]] and/or as [[HttpSlot]].
  * To achieve a single implementation featuring both Slot and HttSlot, first create an abstract class extending OperationSlot
  * which implements the operation, then create two subclasses with one extending [[JsonSlot]] and the other one [[HttpJsonSlot]].
  *
  * @param globals implicit reference to the [[PluginGlobals]] implementation, for the reference to the chunk helper
  * @tparam A type argument for the request type. If the operation has no input parameters, use [[Void]] as type
  */
abstract class OperationSlot[A: ClassTag : TypeTag](implicit globals: PluginGlobals) extends JsonOperation with JsonFormats with PluginLogger with JsonChunkSupport {

  import OperationSlot._

  override def chunkHelper: ChunkHelper = globals.chunkHelper

  override def result(item: JValue)(implicit userSession: UserSession): JValue = {
    val result = Try(parseArgument(item)) match {
      case Failure(thr) =>
        val arg = if (item == JNothing) "[Nothing]" else compact(render(item))
        log.debug("Error decoding argument: " + arg + " with message: " + thr.getMessage)
        OperationError(badRequestMessage)
      case Success(arg) => Try(operation(arg)) match {
        case Failure(RestMessageException(msg)) => OperationError(operationErrorMessage(msg))
        case Failure(OperationException(restMsg)) => OperationError(restMsg)
        case Failure(other) =>
          log.error("Error in operation " + getClass, other)
          OperationError(operationErrorMessage("Unexpected Error"))
        case Success(res) => OperationSuccess(Some(res).filter(!_.isInstanceOf[Unit]))
      }
    }
    decompose(result)
  }

  /**
    * Get operation error for a raised error message. Override to define a customized error code for general errors.
    *
    * @param msg error message
    * @return error message with error code
    */
  def operationErrorMessage(msg: String): RestMessage = RestMessage(msg, "")

  lazy val badRequestMessage: RestMessage = operationErrorMessage("Unable to parse argument")

  /**
    * Try to parse parameter JSON to parameter class.
    *
    * @param item JSON parameter
    * @return parsed parameter
    */
  def parseArgument(item: JValue): A = {
    if (typeOf[A] =:= typeOf[Void]) {
      null.asInstanceOf[A]
    } else if (item == JNull) {
      throw OperationException(badRequestMessage)
    } else  {
      extract[A](item)
    }
  }

  /**
    * Operation to execute.
    *
    * @param arg    parsed parameter
    * @param userSession user session
    * @return template operation result
    */
  def operation(arg: A)(implicit userSession: UserSession): Any

}
