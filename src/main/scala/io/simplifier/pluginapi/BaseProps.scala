package io.simplifier.pluginapi

import akka.actor.Props
import io.simplifier.pluginapi.configuration.ConfigurationRegisterRequest
import io.simplifier.pluginapi.permissions.PluginPermissionObject
import io.simplifier.pluginapi.slots.{EmptyAssetHandler, Slot}
import org.json4s.JValue

/**
 * Trait for properties of plugin base slot.
 */
trait BaseProps {

  /** Abstract def for properties */
  def props: Props = Props(classOf[PluginSlot], this)

  /** Abstract collection of all async slots */
  val slots: Map[String, Props]

  /** Abstract collection of all sync slots */
  val httpSlots: Map[String, Props]

  /** List of all permissions managed by the plugin (default: empty). */
  val permissionObjects: Seq[PluginPermissionObject] = Seq.empty

  /** Actor props for asset handler (default: no assets) */
  val assets: Props = Props[EmptyAssetHandler]

  /** Configuration for AdminUI (default: No Admin View) */
  def configuration(pluginName: String): Option[ConfigurationRegisterRequest] = None

  /** Set of names of Slots/HttpSlots, which are only allowed to be executed in Standalone (=No Cluster) Mode or on the Primary Cluster server */
  val standaloneOnlySlots: Option[Set[String]] = None

}

/**
 * Plugin Slot class.
 * @param baseProps given baseProps implementation for the plugin.
 */
class PluginSlot(baseProps: BaseProps) extends Slot {

  val slots = for ((k, v) <- baseProps.slots) yield (k -> context.actorOf(v, k))
  val httpSlots = for ((k, v) <- baseProps.httpSlots) yield (k -> context.actorOf(v, k))
  val assetHandler = context.actorOf(baseProps.assets, "ASSET")

  override def slot(param: JValue)(implicit userSession: UserSession) = {
    println("Plugin Slot called.")
  }

}
