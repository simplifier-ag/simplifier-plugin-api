package io.simplifier.pluginapi.helper

import org.apache.commons.codec.binary.Base64

/**
 * Trait providing Base64 Encoding/Decoding.
 * @author Christian Simon
 */
trait Base64Encoding {

  private[this] val base64Codec = new Base64()

  /**
   * Encode Byte Data to Base64 String.
   * @param value byte data
   * @return Base64 String
   */
  protected def encodeB64(value: Array[Byte]): String = base64Codec encodeToString value

  /**
   * Decode Base64 String to Byte Data.
   * @param value Base64 String
   * @return byte data
   */
  protected def decodeB64(value: String): Array[Byte] = base64Codec decode value

}