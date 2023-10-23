package io.simplifier.pluginapi.slots

import akka.actor.Actor
import io.simplifier.pluginapi.{AssetNotFound, AssetRequest, HttpPostResponse, PluginMessage}
import io.simplifier.pluginapi.helper.PluginLogger
import org.apache.commons.io.{FilenameUtils, IOUtils}

/**
 * Trait for asset handler in plugin.
 * @author Christian Simon
 */
trait AssetHandler extends Actor {

  /**
   * Abstract handler for asset.
   * @param path asset url
   * @return either a response or None for missing assets
   */
  def handleAsset(path: String): Option[HttpPostResponse]

  protected def handleAssetResult(path: String): PluginMessage =
    handleAsset(path) getOrElse { AssetNotFound }

  def receive: Receive = {
    case AssetRequest(path) =>
      sender ! handleAssetResult(path)
    case other => println(s"error, invalid request: $other")
  }

}

/**
 * Asset handler which does not return any assets.
 */
class EmptyAssetHandler extends AssetHandler {
  
  override def handleAsset(path: String): Option[HttpPostResponse] = None
  
}

/**
  * Abstract supertype for plugin asset handlers that serve resources from the classpath.
  */
abstract class ResourceAssetHandler extends ChunkedAssetHandler with PluginLogger {

  /**
    * Path inside the resources folder to serve as assets (overwrite for different path)
    */
  val assetResourcePath: String = "assets"

  /**
    * Map of MIME types by extension (override if you need additional types)
    */
  val mimeExtensionMap: Map[String, String] = Map(
    "json" -> "application/json",
    "js" -> "application/javascript",
    "properties" -> "text/plain",
    "jpg" -> "image/jpeg",
    "jpeg" -> "image/jpeg",
    "png" -> "image/png",
    "gif" -> "image/gif"
  )

  /**
    * Get MIME Type by extension of path.
    * @param path path to check
    * @return MIME type, or "application/octet-stream" is not recognized
    */
  def mimeByExt(path: String): String = {
    val ext = FilenameUtils.getExtension(path).toLowerCase
    mimeExtensionMap.get(ext) match {
      case Some(mime) => mime
      case None =>
        log.warn("Unknown extension for " + path)
        "application/octet-stream"
    }
  }

  /**
    * Load asset from resource.
    * @param path path to load
    * @param mime MIME type
    * @return resource as option of [[HttpPostResponse]]
    */
  def fromResource(path: String, mime: String): Option[HttpPostResponse] = {
    val maybeUrl = Option(getClass.getClassLoader.getResource(assetResourcePath + "/" + path))
    maybeUrl map {
      url =>
        HttpPostResponse(mime, IOUtils.toByteArray(url))
    }
  }

  override def handleAsset(path: String): Option[HttpPostResponse] = {
    val mime = mimeByExt(path)
    val response = fromResource(path, mime)
    if (response.isEmpty) {
      log.info(s"Asset Not Found: $path")
    }
    response
  }

}