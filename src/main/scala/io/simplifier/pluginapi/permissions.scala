package io.simplifier.pluginapi

import io.simplifier.pluginapi.rest.PluginApiMessage


/**
  * Definition of a permission object.
 *
  * @param name display name
  * @param technicalName (unique) technical name
  * @param description description / tooltip text
  * @param possibleCharacteristics characteristics for the permission object (key is technical name)
  */
case class PermissionObjectDefinition(
                                       name: String,
                                       technicalName: String,
                                       description: String,
                                       possibleCharacteristics: Map[String, PermissionObjectCharacteristicDefinition]
                                     )

/**
  * Definition of a permission object characteristic.
  * @param name display name
  * @param description description / tooltip
  * @param possibleValues enumerated list of possible value, if applicable
  * @param default default value
  * @param displayType type of rendered control in the UI: one of "checkbox", "dropdown" or "textfield"
  */
case class PermissionObjectCharacteristicDefinition(
                                                     name: String,
                                                     description: String,
                                                     possibleValues: Option[Seq[String]],
                                                     default: String,
                                                     displayType: String
                                                   )

/**
  * Instance of a permission object, granted to a role of a user.
  * @param technicalName technical name of permission object
  * @param characteristics key-value implementation of the permission object characteristics,
  *                        where the key is the technical name of the characteristic and the value is
  *                        the value if the characteristic granted to the role
  */
case class GrantedPermission(
                              technicalName: String,
                              characteristics: Map[String, Set[String]]
                            )

/**
  * Multiple granted permission objects. For each permission object in each role of a user there will be an item in the sequence.
  * @param permissionObjects sequence of granted permission objects
  */
case class GrantedPermissions(
                               permissionObjects: Seq[GrantedPermission]
                             ) extends PluginApiMessage

case class RoleNamesRequest(
                      pluginName: String,
                      roleNames: Set[String]
                    ) extends PluginApiMessage

case class RoleNames(
                roleNames: Set[String]
                ) extends PluginApiMessage

case class UserOrAppName(
                        userOrAppName: String
                        ) extends PluginApiMessage