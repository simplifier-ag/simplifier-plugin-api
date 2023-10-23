package io.simplifier.pluginapi

import org.json4s.JValue

/** base message base class */
sealed trait PluginMessage

/**
  * Ping message
  */
case class Ping() extends PluginMessage

/**
  * JSON message
  *
  * this is std message for async request
  * @param item payload
  * @param userSession token/userId of current session
  */
case class JSON(item: JValue, userSession: UserSession) extends PluginMessage

/**
  * JSON message (chunked).
  *
  * this is std message fÃ¼r async request (chunked is data too large for single transmission).
  * @param chunkSession unique ID for the chunks to be transmitted
  * @param chunk number of current chunk (starting from 1)
  * @param chunks total number of chunks
  * @param data byte data of current chunk
  * @param userSession token/userId of current session
  * @param checksum MD5 checksum to verify the integrity of the whole transmission
  */
case class JSONChunked(chunkSession: String, chunk: Int, chunks: Int, data: Array[Byte], userSession: UserSession, checksum: String) extends PluginMessage

/**
  * Request to retrieve a chunk of a JSON message.
  * @param chunkSession session id to request a chunk for
  * @param chunk chunk number to request (starting with 1)
  * @param last flag if the request is the last one (and the cached chunks for this session should be removed after the request)
  */
case class JSONChunkedRequest(chunkSession: String, chunk: Int, last: Boolean) extends PluginMessage

/** acknowledge response */
case class AKN() extends PluginMessage

/**
  * HttpPost message
  *
  * this is std message for sync request
  * @param item
  * @param userSession token/userId of current session
  */
case class HttpPost(item: JValue, userSession: UserSession) extends PluginMessage

/**
  * HttpPost message (chunked).
  *
  * @param chunkSession unique ID for the chunks to be transmitted
  * @param chunk number of current chunk (starting from 1)
  * @param chunks total number of chunks
  * @param data byte data of current chunk
  * @param userSession token/userId of current session
  * @param checksum MD5 checksum to verify the integrity of the whole transmission
  */
case class HttpPostChunked(chunkSession: String, chunk: Int, chunks: Int, data: Array[Byte], userSession: UserSession, checksum: String) extends PluginMessage

/**
  * HttpPostResonse
  *
  * std message for sync response
  * @param mimeType
  * @param content
  */
case class HttpPostResponse(mimeType: String, content: Array[Byte]) extends PluginMessage

/**
  * HttpPostResonse message (chunked).
  *
  * @param mimeType mime type of the HttpPostResonse
  * @param chunkSession unique ID for the chunks to be transmitted
  * @param chunk number of current chunk (starting from 1)
  * @param chunks total number of chunks
  * @param data byte data of current chunk
  * @param checksum MD5 checksum to verify the integrity of the whole transmission
  */
case class HttpPostResponseChunked(mimeType: String, chunkSession: String, chunk: Int, chunks: Int, data: Array[Byte], checksum: String) extends PluginMessage

/**
  * Request to retrieve a chunk of a HttpPostResponse message.
  * @param chunkSession session id to request a chunk for
  * @param chunk chunk number to request (starting with 1)
  * @param last flag if the request is the last one (and the cached chunks for this session should be removed after the request)
  */
case class HttpPostResponseChunkedRequest(chunkSession: String, chunk: Int, last: Boolean) extends PluginMessage

/**
  * killme message
  *
  * this is only for testing purpose
  */
case class KillMe() extends PluginMessage // <- SECURITY

case class plugin(name: String, slotName: String)

/**
  * redirect to plugin Message
  *
  * send message from one plugin to another
  * !not tested yet!
  *
  * @param plugin
  * @param item
  * @param userSession token/userId of current session
  */
case class RedirectToPlugin(plugin: plugin, item: JValue, userSession: UserSession) extends PluginMessage

/**
  * redirect to plugin Message (chunked).
  *
  * @param plugin plugin params
  * @param chunkSession unique ID for the chunks to be transmitted
  * @param chunk number of current chunk (starting from 1)
  * @param chunks total number of chunks
  * @param data byte data of current chunk
  * @param userSession token/userId of current session
  * @param checksum MD5 checksum to verify the integrity of the whole transmission
  */
case class RedirectToPluginChunked(plugin: plugin, chunkSession: String, chunk: Int, chunks: Int, data: Array[Byte], userSession: UserSession, checksum: String) extends PluginMessage

/**
  * error handler for redirects
  *
  * @param msg
  */
case class ErrorMessage(msg: String) extends PluginMessage

/**
  * Request for asset.
  * @param path asset path
  */
case class AssetRequest(path: String) extends PluginMessage

/**
  * message to indicate a requested asset has not been found.
  */
case object AssetNotFound extends PluginMessage

