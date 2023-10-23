package io.simplifier.pluginapi

import org.json4s.{DefaultFormats, JNothing, JValue}
import org.json4s.jackson.JsonMethods.parseOpt
import org.json4s.jackson.Serialization

import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.UUID

/**
 * Utility for for splitting and combining chunked data.
 * @author Christian Simon
 */
trait ChunkHelper {

  /** Maximal chunk size (in bytes) allowed in a packet. */
  val chunkSize: Int

  private val UTF_8 = Charset.forName("UTF-8")

  implicit val formats = DefaultFormats

  /**
   * Split JSON plugin message into chunks, if necessary.
   * @param json JSON value
   * @return either the unsplitted JSON or a sequence of chunks
   */
  def splitJsonMessage(json: JValue, userSession: UserSession): Either[JSON, Seq[JSONChunked]] = {
    val bytes = getBytes(json)
    val chunks = splitInChunks(bytes)
    if (chunks.length > 1) {
      val session = UUID.randomUUID.toString
      val checksum = mkChecksum(bytes)
      val jsonChunks = Range(1, chunks.length + 1) map {
        chunk => JSONChunked(session, chunk, chunks.length, chunks(chunk - 1), userSession, checksum)
      }
      Right(jsonChunks)
    } else
      Left(JSON(json, userSession))
  }

  /**
   * Split redirectToPlugin plugin message into chunks, if necessary.
   * @param redirect redirectToPlugin message
   * @return either the unsplitted redirectToPlugin or a sequence of chunks
   */
  def splitRedirectMessage(redirect: RedirectToPlugin): Either[RedirectToPlugin, Seq[RedirectToPluginChunked]] = {
    val bytes = getBytes(redirect.item)
    val chunks = splitInChunks(bytes)
    if (chunks.length > 1) {
      val session = UUID.randomUUID.toString
      val checksum = mkChecksum(bytes)
      val redirectChunks = Range(1, chunks.length + 1) map {
        chunk => RedirectToPluginChunked(redirect.plugin, session, chunk, chunks.length, chunks(chunk - 1), redirect.userSession, checksum)
      }
      Right(redirectChunks)
    } else
      Left(redirect)
  }

  /**
   * Split HttpPost plugin message into chunks, if necessary.
   * @param httpPost HttpPost message
   * @return either the unsplitted HttpPost or a sequence of chunks
   */
  def splitHttpPostMessage(httpPost: HttpPost): Either[HttpPost, Seq[HttpPostChunked]] = {
    val bytes = getBytes(httpPost.item)
    val chunks = splitInChunks(bytes)
    if (chunks.length > 1) {
      val session = UUID.randomUUID.toString
      val checksum = mkChecksum(bytes)
      val redirectChunks = Range(1, chunks.length + 1) map {
        chunk => HttpPostChunked(session, chunk, chunks.length, chunks(chunk - 1), httpPost.userSession, checksum)
      }
      Right(redirectChunks)
    } else
      Left(httpPost)
  }

  /**
   * Split HttpPostResponse plugin message into chunks, if necessary.
   * @param response HttpPostResponse message
   * @return either the unsplitted HttpPostResponse or a sequence of chunks
   */
  def splitHttpPostResponseMessage(response: HttpPostResponse): Either[HttpPostResponse, Seq[HttpPostResponseChunked]] = {
    val bytes = response.content
    val chunks = splitInChunks(bytes)
    if (chunks.length > 1) {
      val session = UUID.randomUUID.toString
      val checksum = mkChecksum(bytes)
      val redirectChunks = Range(1, chunks.length + 1) map {
        chunk => HttpPostResponseChunked(response.mimeType, session, chunk, chunks.length, chunks(chunk - 1), checksum)
      }
      Right(redirectChunks)
    } else
      Left(response)
  }

  /**
   * Check if array of chunks is complete.
   * @param chunks array of chunks
   * @return true if the array is complete, false otherwise
   */
  def chunksFinished(chunks: Array[JSONChunked]) =
    chunks.forall { _ != null }

  /**
   * Check if array of chunks is complete.
   * @param chunks array of chunks
   * @return true if the array is complete, false otherwise
   */
  def chunksFinished(chunks: Array[RedirectToPluginChunked]) =
    chunks.forall { _ != null }

  /**
   * Check if array of chunks is complete.
   * @param chunks array of chunks
   * @return true if the array is complete, false otherwise
   */
  def chunksFinished(chunks: Array[HttpPostChunked]) =
    chunks.forall { _ != null }

  /**
   * Check if array of chunks is complete.
   * @param chunks array of chunks
   * @return true if the array is complete, false otherwise
   */
  def chunksFinished(chunks: Array[HttpPostResponseChunked]) =
    chunks.forall { _ != null }

  /**
   * Combine chunks.
   * @param chunks array of chunk data
   * @return Some JSON if the chunks are complete and the checksum matches, otherwise None
   */
  def joinChunks(chunks: Array[JSONChunked]): Option[JSON] =
    if (chunksFinished(chunks)) {
      val storedChecksum = chunks(0).checksum
      val userSession = chunks(0).userSession
      val chunkBytes = chunks.map { _.data }
      val bytes = chunkBytes.flatten
      val checksum = mkChecksum(bytes)
      if (checksum == storedChecksum) {
        parseOpt(new String(bytes, UTF_8)) map { JSON(_, userSession) }
      } else {
        println(s"Warning: Checksum mismatch: $storedChecksum != $checksum")
        None
      }
    } else None

  /**
   * Combine chunks.
   * @param chunks array of chunk data
   * @return Some redirectToPlugin if the chunks are complete and the checksum matches, otherwise None
   */
  def joinChunks(chunks: Array[RedirectToPluginChunked]): Option[RedirectToPlugin] =
    if (chunksFinished(chunks)) {
      val plugin = chunks(0).plugin
      val storedChecksum = chunks(0).checksum
      val userSession = chunks(0).userSession
      val chunkBytes = chunks.map { _.data }
      val bytes = chunkBytes.flatten
      val checksum = mkChecksum(bytes)
      if (checksum == storedChecksum) {
        parseOpt(new String(bytes, UTF_8)) map { RedirectToPlugin(plugin, _, userSession) }
      } else {
        println(s"Warning: Checksum mismatch: $storedChecksum != $checksum")
        None
      }
    } else None

  /**
   * Combine chunks.
   * @param chunks array of chunk data
   * @return Some HttpPost if the chunks are complete and the checksum matches, otherwise None
   */
  def joinChunks(chunks: Array[HttpPostChunked]): Option[HttpPost] =
    if (chunksFinished(chunks)) {
      val storedChecksum = chunks(0).checksum
      val userSession = chunks(0).userSession
      val chunkBytes = chunks.map { _.data }
      val bytes = chunkBytes.flatten
      val checksum = mkChecksum(bytes)
      if (checksum == storedChecksum) {
        parseOpt(new String(bytes, UTF_8)) map { HttpPost(_, userSession) }
      } else {
        println(s"Warning: Checksum mismatch: $storedChecksum != $checksum")
        None
      }
    } else None

  /**
   * Combine chunks.
   * @param chunks array of chunk data
   * @return Some HttpPostResponse if the chunks are complete and the checksum matches, otherwise None
   */
  def joinChunks(chunks: Array[HttpPostResponseChunked]): Option[HttpPostResponse] =
    if (chunksFinished(chunks)) {
      val mimeType = chunks(0).mimeType
      val storedChecksum = chunks(0).checksum
      val chunkBytes = chunks.map { _.data }
      val bytes = chunkBytes.flatten
      val checksum = mkChecksum(bytes)
      if (checksum == storedChecksum) {
        Some(HttpPostResponse(mimeType, bytes))
      } else {
        println(s"Warning: Checksum mismatch: $storedChecksum != $checksum")
        None
      }
    } else None

  /**
   * Split byte array into sequence of chunks.
   * @param bytes byte array
   * @return sequence of byte arrays
   */
  private def splitInChunks(bytes: Array[Byte]): Seq[Array[Byte]] = (bytes grouped chunkSize).toSeq

  /**
   * Get bytes of JSON value.
   * @param jvalue input JSON
   * @return byte array containing the JSON
   *
   */
  private def getBytes(jvalue: JValue) = jvalue match {
    case JNothing => new Array[Byte](0)
    case other    => Serialization.write(other).getBytes(UTF_8)
  }

  /**
   * Create MD5 checksum from byte array.
   * @param data byte array
   * @return MD5 checksum as String
   */
  private def mkChecksum(data: Array[Byte]): String = {
    val messageDigest = MessageDigest getInstance "MD5"
    val digest = messageDigest digest data
    digest.map("%02X".format(_)).mkString
  }

}
