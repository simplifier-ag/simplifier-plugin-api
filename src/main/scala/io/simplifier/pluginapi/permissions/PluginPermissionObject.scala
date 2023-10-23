package io.simplifier.pluginapi.permissions

import org.json4s.{JField, JObject}
import org.json4s.JsonDSL._

/**
 * Plugin permission object.
 * @author Christian Simon
 */
trait PluginPermissionObject {

  /**
   * Name of the permission object.
   */
  val name: String

  /**
   * Technical Name of the permission object.
   */
  val technicalName: String

  /**
   * Description of the permission object.
   */
  val description: String

  /**
   * Possible characteristics for the admin ui.
   */
  val possibleCharacteristics: JObject


  /**
    * Base trait for characteristic definition.
    */
  sealed trait CharacteristicDefinition

  /**
    * Boolean characteristic, rendered as CheckBox.
    */
  case class CheckboxCharacteristic(technicalName: String, name: String, description: String, defaultValue: Boolean = true) extends CharacteristicDefinition

  /**
    * Enum characteristic, rendered as DropDown.
    */
  case class DropDownCharacteristic(technicalName: String, name: String, description: String, values: Seq[String], defaultValue: String) extends CharacteristicDefinition

  /**
    * FreeText characteristic, rendered as TextField.
    */
  case class TextfieldCharacteristic(technicalName: String, name: String, description: String, defaultValue: String = "") extends CharacteristicDefinition

  /**
    * Generate JSON Characteristics from CharacteristicDefinitions.
    * @param definitions definitions as case classes
    * @return definitions as [[JObject]]
    */
  protected def genCharacteristics(definitions: CharacteristicDefinition*): JObject = {
    "characteristics" -> JObject(definitions.toList.map(genCharacteristic))
  }

  /**
    * Generate JSON Characteristic from CharacteristicDefinition.
    * @param definition single characteristic definition
    * @return definition as [[JField]]
    */
  private[this] def genCharacteristic(definition: CharacteristicDefinition): JField = {
    definition match {
      case CheckboxCharacteristic(cTechnicalName, cName, cDescription, defVal) => JField(cTechnicalName,
        ("name" -> cName) ~
          ("description" -> cDescription) ~
          ("possibleValues" -> List(true, false)) ~
          ("default" -> defVal) ~
          ("displayType" -> "checkbox"))
      case DropDownCharacteristic(cTechnicalName, cName, cDescription, values, defVal) => JField(cTechnicalName,
        ("name" -> cName) ~
          ("description" -> cDescription) ~
          ("possibleValues" -> values) ~
          ("default" -> defVal) ~
          ("displayType" -> "dropdown"))
      case TextfieldCharacteristic(cTechnicalName, cName, cDescription, defVal) => JField(cTechnicalName,
        ("name" -> cName) ~
          ("description" -> cDescription) ~
          ("default" -> defVal) ~
          ("displayType" -> "textfield"))
    }
  }

}