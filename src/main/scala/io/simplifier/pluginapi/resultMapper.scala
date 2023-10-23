package io.simplifier.pluginapi

trait resultMapper {

  import org.json4s._

  type RESULT
  type ERROR <: RESULT
  type SUCCESS <: RESULT
  protected val successTrigger:String

  def resultFromJson(json: JValue)(implicit s: Manifest[SUCCESS], e: Manifest[ERROR]): Option[RESULT] = {
    implicit val formats = DefaultFormats
    try {
      if ((json \ successTrigger).extract[Boolean])
        Some(json.extract[SUCCESS])
      else
        Some(json.extract[ERROR])
    }
    catch {
      case e: Exception => None
    }
  }

  def resultFromJson(optjson: Option[JValue])(implicit s: Manifest[SUCCESS], e: Manifest[ERROR]): Option[RESULT] = optjson flatMap (json => resultFromJson(json))
}
