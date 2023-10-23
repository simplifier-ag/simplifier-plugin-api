package io.simplifier.pluginapi.helper

import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.JValue

/**
 * Helper objects for JSOn extraction in case statements.
 * @author Christian Simon
 */
object JSONExtractionHelper {

  implicit val formats: Formats = DefaultFormats

  /**
   * Extractor class to extract values from JSON in case statements.
   */
  class ExtractJsonResponse[A](implicit manifest: Manifest[A]) {
    def unapply(json: JValue): Option[A] = Extraction.extractOpt(json)
  }

}