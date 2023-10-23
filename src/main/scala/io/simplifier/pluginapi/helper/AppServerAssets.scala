package io.simplifier.pluginapi.helper

import io.simplifier.pluginapi.PluginGlobals
import io.simplifier.pluginapi.helper.JSONExtractionHelper.ExtractJsonResponse
import io.simplifier.pluginapi.UserSession

/**
 * Extension to Globals to retrieve assets from the AppServer
 * @author Christian Simon
 */
trait AppServerAssets extends Base64Encoding {

  self: PluginGlobals =>

  import AppServerAssets._

  def getAppServerAsset(name: String): Option[Array[Byte]] = {
    val request = AssetRequest(name)
    implicit val userSession: UserSession = UserSession.unauthenticated
    val initialResponse = queryServer(assetsSlot, request) {
      case ExtractAssetResult(response) => response
    }
    initialResponse flatMap {
      case AssetResponse(false, _)             => None
      case AssetResponse(true, Some(fullData)) => Some(decodeB64(fullData))
      case other =>
        log.warn(s"Got unexpected response for asset: $other")
        None
    }
  }

}

object AppServerAssets {

  val assetsSlot = "assets"

  case class AssetRequest(name: String)
  case class AssetResponse(found: Boolean, data: Option[String])

  object ExtractAssetResult extends ExtractJsonResponse[AssetResponse]

}