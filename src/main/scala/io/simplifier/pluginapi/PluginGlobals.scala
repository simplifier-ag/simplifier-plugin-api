package io.simplifier.pluginapi

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import com.fasterxml.jackson.core.JsonParseException
import com.typesafe.config._
import io.simplifier.pluginapi.configuration.ConfigurationRegisterRequest
import io.simplifier.pluginapi.helper.PluginLogger
import io.simplifier.pluginapi.permissions.{PermissionQueryRequest, PermissionQueryResult, PermissionRegisterRequest, PluginPermissionObject}
import io.simplifier.pluginapi.slots.JsonChunkSupport
import org.json4s.JsonDSL._
import org.json4s.ParserUtil.ParseException
import org.json4s._
import org.json4s.jackson.JsonMethods._

import java.io.File
import scala.language.postfixOps
import scala.reflect.runtime.universe.{TypeTag, typeOf}

/**
 * Abstract super class for GlobalSettings. No customization required.
 * Use it like this:
 * object GlobalSettings extends PluginGlobalSettings
 */
abstract class PluginGlobalSettings {

  private[this] val defaultSettings = "settings"

  private[this] var settingsSource: Option[String] = None

  /**
   * Initialize the source for the settings from commandline arguments.
   * @param args args from main() method
   */
  def initSettingsFromCommandline(args: Array[String]): Unit = {
    if (args != null)
      settingsSource = args.headOption
    else
      settingsSource = None
  }

  /**
   * Load settings from configured source.
   */
  def loadSettings: Config = settingsSource match {
    case None => ConfigFactory.load(defaultSettings)
    case Some(name) =>
      val fullName = if (name endsWith ".conf") name else s"$name.conf"
      val configFile = new File(fullName)
      if (configFile.exists) {
        ConfigFactory.parseFileAnySyntax(configFile)
      } else {
        ConfigFactory.load(
          if (getClass.getClassLoader.getResource(fullName) != null)
            fullName
          else
            defaultSettings)
      }
  }

}

/**
 * Abstract superclass for Globals in a Plugin.
 * @author Christian Simon
 */
abstract class PluginGlobals(globalSettings: PluginGlobalSettings) extends PluginLogger with JsonChunkSupport {

  import PluginGlobals._

  /*
   * Settings
   */

  val settings: Config = globalSettings.loadSettings

  def getSettingString(key: String): Option[String] = if (settings.hasPath(key)) Some(settings.getString(key)) else None

  def getSettingInt(key: String): Option[Int] = if (settings.hasPath(key)) Some(settings.getInt(key)) else None

  def getSettingLong(key: String): Option[Long] = if (settings.hasPath(key)) Some(settings.getLong(key)) else None
  
  /** Chunk Helper for Plugin. */
  val chunkHelper: ChunkHelper = new ChunkHelper {

    /** ChunkSize taken from settings key "akkaServer.file_chunk_size". */
    val chunkSize: Int =
      if (settings.hasPath("akkaServer.file_chunk_size"))
        settings.getInt("akkaServer.file_chunk_size")
      else
        100000
  }

  val registrationSecret: String = "5tvWLLQDyFpjnr2jaAwOLWU0nxfwz6Xf7UykUhwwHN5H1mkx5CFRxJO4BAx" + new String(Array[Byte](83, 99, 53, 76, 57))
  
  /*
   * Plugin State
   */

  implicit val formats: Formats = DefaultFormats
  
  protected var system: Option[ActorSystem] = None

  protected var baseSlot: Option[ActorRef] = None

  protected var serverJSON: JValue = JNothing

  protected var appServerReceive: Option[appServerSlot] = None

  protected def getAppServerSlot(slot: String): Option[ActorSelection] = (appServerReceive, system) match {
    case (Some(r), Some(s)) => Some(s.actorSelection(r.getUrl(slot)))
    case _                  => None
  }

  protected def getAppServerSlot: Option[ActorSelection] = (appServerReceive, system) match {
    case (Some(r), Some(s)) => Some(s.actorSelection(r.getUrl))
    case _                  => None
  }

  /**
   * send message to appServer slot
   *
   * @param msg   message to be send
   * @param slot  appServer slot name to send to
   */
  def sendToServer(msg: JSON, slot: String): Unit = getAppServerSlot(slot).foreach {
    _ !! msg
  }

  /**
   * send message sync to appServer slot
   *
   * @param msg   message to be send
   * @param slot  appServer slot name to send to
   * @return      Option of PluginMessage
   */
  def requestServerSync(msg: PluginMessage, slot: String): Option[JValue] = {
    import akka.util.Timeout

    import java.util.concurrent.TimeoutException
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    import scala.concurrent.{Await, Future}
    implicit val timeout: Timeout = Timeout(settings.getLong("akkaServer.timeoutSeconds") seconds)
    system match {
      case Some(s) =>
        getAppServerSlot(slot).flatMap {
          appServer =>
            val future: Future[Option[JValue]] =
              (appServer ?? msg).map {
                case jvalue: JValue => Some(jvalue)
                case JSON(jvalue, _)   => Some(jvalue)
                case other =>
                  log.warn(s"Got $other")
                  None
              } recover {
                case e: TimeoutException =>
                  None
              }
            Await.result(future, timeout.duration)
        }
      case _ =>
        None
    }
  }

  /**
   * send message sync to appServer slot
   */
  def requestServerSync(msg: io.simplifier.pluginapi.PluginMessage): Option[JValue] = {
    import akka.util.Timeout

    import java.util.concurrent.TimeoutException
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    import scala.concurrent.{Await, Future}
    implicit val timeout: Timeout = Timeout(settings.getLong("akkaServer.timeoutSeconds") seconds)
    system match {
      case Some(s) =>
        getAppServerSlot.flatMap { appServer =>
          val future: Future[Option[PluginMessage]] =
            (appServer ?? msg).mapTo[PluginMessage] map (Some(_)) recover {
              case e: TimeoutException =>
                None
            }
          val res = Await.result(future, timeout.duration)
          res map {
            case JSON(j, _) => j
            case other =>
              log.warn(s"Got $other")
              JNothing
          }
        }
      case _ =>
        None
    }
  }

  /**
   * Check user permissions on the appserver.
   * @param permissionObjectName optional PermissionObject name to filter for
   * @return query result (contains list of found PermissionObjects with characteristics)
   */
  def getPermissions(permissionObjectName: Option[String] = None)(implicit userSession: UserSession): PermissionQueryResult = {
    val msg = permissionObjectName map PermissionQueryRequest map {
      Extraction.decompose(_)
    } getOrElse JNothing
    requestServerSync(JSON(msg, userSession), "checkPermission") match {
      case Some(jvalue) =>
        Extraction.extractOpt[PermissionQueryResult](jvalue) match {
          case Some(result) => result
          case None =>
            log.error(s"Error fetching permissions for user session $userSession, result was: $jvalue")
            PermissionQueryResult(Seq())
        }
      case None =>
        log.error(s"Error fetching permissions for user session $userSession")
        PermissionQueryResult(Seq())
    }
  }

  /**
   * get setting retrieved from appServer by registration
   * @return appServerSlot
   */
  def appServerSettings: Option[appServerSlot] = if (serverJSON != JNothing) {
    try {
      Some((serverJSON \ "appServer").extract[appServerSlot])
    } catch {
      case e: Throwable => None
    }
  } else None

  protected var props: Option[BaseProps] = None

  /**
   * Register plugin at server.
   */
  protected def registerAtServer(registerParams: JValue): Option[JValue] = {
    import dispatch._
    import Defaults._

    import scala.concurrent.duration._
    import scala.concurrent.{Await, TimeoutException}

    val reqUrl = settings.getString("appServerEndpoint.url")
    val req = url(reqUrl).POST <:< Map("PluginRegistrationSecret" -> registrationSecret)
    val reqWithEntity  = (req << compact(render(registerParams))).setContentType("application/json", "utf-8")

    val response: Future[String] = Http(reqWithEntity OK as.String)

    try {
      val res = parse(Await.result(response, settings.getInt("appServerEndpoint.timeoutSeconds") seconds))
      res.extract[jsonResult] match {
        case jsonResult(result, true)  => Some(result)
        case jsonResult(result, false) => Some(JNothing)
      }
    } catch {
      case e: TimeoutException => None
      case e: ParseException   => Some(JNothing)
      case e: JsonParseException   => Some(JNothing)
      case e: MappingException => Some(JNothing)
      case e: Exception =>
        println(e.getMessage)
        None
    }
  }

  protected def collectPluginProperties(p: BaseProps): pluginDescription = pluginDescription(
    settings.getString("akkaServer.host"),
    settings.getLong("akkaServer.port"),
    settings.getString("akkaServer.pluginName"),
    settings.getString("akkaServer.pluginName"),
    p.slots.keys.toList,
    p.httpSlots.keys.toList,
    p.standaloneOnlySlots)

  /**
   * Global plugin init
   *
   * @param p properties of basicActor
   */
  def init(p: BaseProps): Unit = {
    log.info("Init global ....")
    props = Some(p)
    val hostname = settings.getString("akkaServer.host")
    val port = settings.getLong("akkaServer.port")
    val myConfig = ConfigFactory.parseString(
      s"""|akka {
          |  loglevel = "INFO"
          |  loggers = ["akka.event.slf4j.Slf4jLogger"]
          |  actor {
          |    provider = "akka.remote.RemoteActorRefProvider"
          |    allow-java-serialization = on
          |  }
          |  remote {
          |    artery.canonical {
          |      hostname = "$hostname"
          |      port = $port
          |    }
          |    log-sent-messages = off
          |    log-received-messages = off
          |  }
          |}
          |""".stripMargin)

    log.info("Start actor system ...")
    val s = ActorSystem(settings.getString("akkaServer.pluginName"), myConfig)
    system = Some(s)

    log.info("Register base slot ...")
    baseSlot = Some(s.actorOf(p.props, name = settings.getString("akkaServer.pluginName")))

    log.info("Register ...")
    serverJSON = registerAtServer(
      ("action" -> "register") ~
        ("plugin" -> Extraction.decompose(collectPluginProperties(p)))) match {
        case Some(res) => res
        case _         => JNothing
      }
    appServerReceive = appServerSettings

    if (serverJSON == JNothing)
      shutdown()
    else {
      log.debug("Server JSON:\n" + pretty(render(serverJSON)))
      
      val registeredPermissionObjects = registerPermissionObjects(p.permissionObjects)
      
      // Register configuration page, if it exists
      val registeredConfiguration = p.configuration(settings.getString("akkaServer.pluginName")).forall {
        cfg => registerConfigurationView(cfg)
      }

      if (!registeredPermissionObjects || !registeredConfiguration) {
        shutdown()
      }
    }
  }

  /**
   * Register permission objects defined by the plugin on the appserver.
   * @param permissionObjects sequence of permission objects to register
   * @return true if all permission objects were registered successfully (or if the list was empty)
   */
  def registerPermissionObjects(permissionObjects: Seq[PluginPermissionObject]): Boolean =
    permissionObjects.forall {
        po => 
          log.info(s"Register PermissionObject ${po.name}")
          val req = Extraction.decompose(PermissionRegisterRequest(po.technicalName ,po.name, po.description, po.possibleCharacteristics))
          val response = requestServerSync(JSON(req, UserSession.unauthenticated), "registerPermission")
          val successful = response.contains(JString("ok"))
          if (!successful) {
            log.error(s"Error registering Permission Object: ${response.map(r => pretty(render(r)))}")
          }
          successful
      }

  /**
   * Register Configuration page on the AdminUI for the plugin on the appserver.
   * @param cfg configuration register request, containing the adminUI view name for the config page
   * @return true if the configuration page was registered successfully 
   */
  def registerConfigurationView(cfg: ConfigurationRegisterRequest): Boolean = {
    log.info(s"Register Configuration $cfg")
    val req = Extraction.decompose(cfg)
    val response = requestServerSync(JSON(req, UserSession.unauthenticated), "registerConfiguration")
    val successful = response.contains(JString("ok"))
    if (!successful) {
      log.error(s"Error registering configuration: ${response.map(r => pretty(render(r)))}")
    }
    successful
  }

  /**
   * shutdown plugin
   */
  def shutdown(): Unit = {
    log.info("Shutdown ....")
    props match {
      case Some(p) =>
        log.info("Unregister at server ...")
        serverJSON = registerAtServer(
          ("action" -> "unregister") ~
            ("plugin" -> Extraction.decompose(collectPluginProperties(p)))) match {
            case Some(res) => res
            case _         => JNothing
          }
        props = None
      case _ =>
    }
    log.info("Shutdown actor system ...")
    system.foreach(_.terminate())
    System.exit(0)
  }
  
  /*
   * AppServer communication utilities
   */

  /**
   * Send Plugin Request to another Plugin. The type parameter [A] is the type of the expected response value.
   * @param pluginName pluginName of the other plugin
   * @param slot slot of the other plugin
   * @param request request object, that can be decomposed as JSON value
   * @param userSession user session
   * @param pf partial function to interpret the returned JSON value to a response value of type A
   * @return Option of the returned response value
   */
  def queryPlugin[A](pluginName: String, slot: String, request: Any)(pf: PartialFunction[JValue, A])
                    (implicit userSession: UserSession): Option[A] = {
    val item = Extraction.decompose(request)
    val redirectMsg = RedirectToPlugin(plugin(pluginName, slot), item, userSession)
    val responseData = requestServerSync(redirectMsg)
    responseData flatMap (pf.lift apply _)
  }

  /**
    * Send Plugin Request to another Plugin. The type parameter [A] is the type of the expected response value.
    * If response type A == [[Unit]] is provided, no json parsing of the response is performed.
    * @param pluginName pluginName of the other plugin
    * @param slot slot of the other plugin
    * @param request request object, that can be decomposed as JSON value
    * @param userSession user session
    * @return Option of the returned response value
    */
  def queryPluginOpt[A : Manifest : TypeTag](pluginName: String, slot: String, request: Any)
                                            (implicit userSession: UserSession): Option[A] = {
    val item = Extraction.decompose(request)
    val redirectMsg = RedirectToPlugin(plugin(pluginName, slot), item, userSession)
    val responseData = requestServerSync(redirectMsg)
    if (typeOf[A] =:= typeOf[Unit]) {
      responseData map (_ => Unit.asInstanceOf[A])
    } else if (typeOf[A] =:= typeOf[Nothing]) {
      // Type Nothing is inferred, if result is not used
      responseData map (_ => Unit.asInstanceOf[A])
    } else {
      responseData flatMap Extraction.extractOpt[A]
    }
  }

  /**
   * Send AppServer Request. The type parameter [A] is the type of the expected response value.
   * @param slot slot of the appServer
   * @param request request object, that can be decomposed as JSON value
   * @param userSession user session
   * @param pf partial function to interpret the returned JSON value to a response value of type A
   * @return Option of the returned response value
   */
  def queryServer[A](slot: String, request: Any)(pf: PartialFunction[JValue, A])(implicit userSession: UserSession): Option[A] = {
    val item = Extraction.decompose(request)
    val responseData = requestServerSync(JSON(item, userSession), slot)
    responseData flatMap pf.lift
  }

  /**
    * Send AppServer Request. The type parameter [A] is the type of the expected response value.
    * @param slot slot of the appServer
    * @param request request object, that can be decomposed as JSON value
    * @param userSession user session for server query
    * @return Option of the returned response value
    */
  def queryServerOpt[A : Manifest](slot: String, request: Any)(implicit userSession: UserSession): Option[A] = {
    val item = Extraction.decompose(request)
    val responseData = requestServerSync(JSON(item, userSession), slot)
    responseData flatMap Extraction.extractOpt[A]
  }

}

/**
 * Companion Object to PluginGlobals.
 */
object PluginGlobals {

  /*
   * Case classes for AppServer communication
   */

  case class jsonResult(result: JValue, success: Boolean)

  case class pluginDescription(hostname: String, port: Long,
                               actorSystemName: String, pluginName: String,
                               slots: List[String],
                               httpPosts: List[String],
                               standaloneOnlySlots: Option[Set[String]])

  case class appServerSlot(hostname: String, port: Long,
                           actorSystemName: String,
                           pluginName: String,
                           slots: List[String]) {
    def getUrl: String = s"akka://$actorSystemName@$hostname:$port/user/$pluginName"
    def getUrl(slotName: String): String = getUrl + "/" + slotName
  }

}
