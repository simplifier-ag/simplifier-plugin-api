package io.simplifier.pluginapi.rest

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

/**
  * Header definitions for (Plugin --> AppServer) and (AppServer --> Plugin) communication.
  */
object PluginHeaders {

  /**
    * Plugin secret header is basic security mechanism to ensure only authorized plugins can communicate with the
    * appserver and vice versa. The secret has to be specified identically in both the appserver and plugin.
    * @param secret secret value
    */
  case class `Plugin-Secret`(secret: String) extends ModeledCustomHeader[`Plugin-Secret`] {
    override def companion: ModeledCustomHeaderCompanion[`Plugin-Secret`] = `Plugin-Secret`
    override def value(): String = secret
    override def renderInResponses(): Boolean = true
    override def renderInRequests(): Boolean = true
  }
  object `Plugin-Secret` extends ModeledCustomHeaderCompanion[`Plugin-Secret`] {
    override def name: String = "Plugin-Secret"
    override def parse(value: String): Try[`Plugin-Secret`] = Success(new `Plugin-Secret`(value))
  }

  /**
    * Header to pass the uri of the initial call from the Simplifier to the plugin.
    *
    * @note      this is necessary, as the AppServer prefix does not consider network topologies like firewalls, jump-hosts etc.
    * @param uri the original call uri
    */
  case class `Simplifier-Call-Uri`(uri: String) extends ModeledCustomHeader[`Simplifier-Call-Uri`] {
    override def companion: ModeledCustomHeaderCompanion[`Simplifier-Call-Uri`] = `Simplifier-Call-Uri`
    override def value(): String = uri
    override def renderInResponses(): Boolean = false
    override def renderInRequests(): Boolean = true
  }
  object `Simplifier-Call-Uri` extends ModeledCustomHeaderCompanion[`Simplifier-Call-Uri`] {
    override def name: String = "Simplifier-Call-Uri"
    override def parse(value: String): Try[`Simplifier-Call-Uri`] = Success(new `Simplifier-Call-Uri`(value))
  }

  /**
    * Header to transfer the authenticated simplifier user to the plugin.
    * @param userId db id of authenticated user
    */
  case class `Simplifier-User`(userId: Long) extends ModeledCustomHeader[`Simplifier-User`] {
    override def companion: ModeledCustomHeaderCompanion[`Simplifier-User`] = `Simplifier-User`
    override def value(): String = userId.toString
    override def renderInResponses(): Boolean = true
    override def renderInRequests(): Boolean = true
  }
  object `Simplifier-User` extends ModeledCustomHeaderCompanion[`Simplifier-User`] {
    override def name: String = "Simplifier-User"
    override def parse(value: String): Try[`Simplifier-User`] = Try(new `Simplifier-User`(userId = value.toLong))
  }

  /**
    * Header to transfer the Token of the authenticated session to the plugin.
    * @param token Simplifier token
    */
  case class SimplifierToken(token: String) extends ModeledCustomHeader[SimplifierToken] {
    override def companion: ModeledCustomHeaderCompanion[SimplifierToken] = SimplifierToken
    override def value(): String = token
    override def renderInResponses(): Boolean = true
    override def renderInRequests(): Boolean = true
  }
  object SimplifierToken extends ModeledCustomHeaderCompanion[SimplifierToken] {
    override def name: String = "SimplifierToken"
    override def parse(value: String): Try[SimplifierToken] = Try(new SimplifierToken(token = value))
  }

  case class AppName(appName: String) extends ModeledCustomHeader[AppName] {
    override def companion: ModeledCustomHeaderCompanion[AppName] = AppName

    override def value(): String = appName

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object AppName extends ModeledCustomHeaderCompanion[AppName] {
    override def name: String = "AppName"

    override def parse(value: String): Try[AppName] = Try(new AppName(value))
  }

  case class JobName(jobName: String) extends ModeledCustomHeader[JobName] {
    override def companion: ModeledCustomHeaderCompanion[JobName] = JobName

    override def value(): String = jobName

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object JobName extends ModeledCustomHeaderCompanion[JobName] {
    override def name: String = "JobName"

    override def parse(value: String): Try[JobName] = Try(new JobName(value))
  }

  case class ParentPerformanceId(parentPerformanceId: String) extends ModeledCustomHeader[ParentPerformanceId] {
    override def companion: ModeledCustomHeaderCompanion[ParentPerformanceId] = ParentPerformanceId

    override def value(): String = parentPerformanceId

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object ParentPerformanceId extends ModeledCustomHeaderCompanion[ParentPerformanceId] {
    override def name: String = "ParentPerformanceId"

    override def parse(value: String): Try[ParentPerformanceId] = Try(new ParentPerformanceId(value))
  }

  case class ModuleName(moduleName: String) extends ModeledCustomHeader[ModuleName] {
    override def companion: ModeledCustomHeaderCompanion[ModuleName] = ModuleName

    override def value(): String = moduleName

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object ModuleName extends ModeledCustomHeaderCompanion[ModuleName] {
    override def name: String = "ModuleName"

    override def parse(value: String): Try[ModuleName] = Try(new ModuleName(value))
  }

  case class ModuleInterfaceName(moduleInterfaceName: String) extends ModeledCustomHeader[ModuleInterfaceName] {
    override def companion: ModeledCustomHeaderCompanion[ModuleInterfaceName] = ModuleInterfaceName

    override def value(): String = moduleInterfaceName

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object ModuleInterfaceName extends ModeledCustomHeaderCompanion[ModuleInterfaceName] {
    override def name: String = "ModuleInterfaceName"

    override def parse(value: String): Try[ModuleInterfaceName] = Try(new ModuleInterfaceName(value))
  }

  case class ClientBusinessObjectName(clientBusinessObjectName: String) extends ModeledCustomHeader[ClientBusinessObjectName] {
    override def companion: ModeledCustomHeaderCompanion[ClientBusinessObjectName] = ClientBusinessObjectName

    override def value(): String = clientBusinessObjectName

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object ClientBusinessObjectName extends ModeledCustomHeaderCompanion[ClientBusinessObjectName] {
    override def name: String = "ClientBusinessObjectName"

    override def parse(value: String): Try[ClientBusinessObjectName] = Try(new ClientBusinessObjectName(value))
  }

  case class ClientBusinessObjectFunctionName(clientBusinessObjectFunctionName: String) extends ModeledCustomHeader[ClientBusinessObjectFunctionName] {
    override def companion: ModeledCustomHeaderCompanion[ClientBusinessObjectFunctionName] = ClientBusinessObjectFunctionName

    override def value(): String = clientBusinessObjectFunctionName

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object ClientBusinessObjectFunctionName extends ModeledCustomHeaderCompanion[ClientBusinessObjectFunctionName] {
    override def name: String = "ClientBusinessObjectFunctionName"

    override def parse(value: String): Try[ClientBusinessObjectFunctionName] = Try(new ClientBusinessObjectFunctionName(value))
  }

  case class IsInternalUser(isInternalUser: Boolean) extends ModeledCustomHeader[IsInternalUser] {
    override def companion: ModeledCustomHeaderCompanion[IsInternalUser] = IsInternalUser

    override def value(): String = isInternalUser.toString

    override def renderInRequests(): Boolean = true

    override def renderInResponses(): Boolean = true
  }

  object IsInternalUser extends ModeledCustomHeaderCompanion[IsInternalUser] {
    override def name: String = "IsInternalUser"

    override def parse(value: String): Try[IsInternalUser] = Try(new IsInternalUser(value.toBoolean))
  }

  sealed trait RequestSource {
    def toInitial: RequestSourceInitial

    def getUri: Option[Uri]
  }

  case class AppServer(initialSource: Option[Uri]) extends RequestSource {
    override def toInitial: RequestSourceInitial = RequestSourceAppServer

    override def getUri: Option[Uri] = initialSource
  }
  case class AppServerDirect(initialSource: Option[Uri]) extends RequestSource {
    override def toInitial: RequestSourceInitial = RequestSourceAppServerDirect

    override def getUri: Option[Uri] = initialSource
  }
  case class BusinessObject(boName: String, initialSource: Option[Uri]) extends RequestSource {
    override def toInitial: RequestSourceInitial = RequestSourceBusinessObject(boName)

    override def getUri: Option[Uri] = initialSource
  }
  case class Plugin(pluginName: String, appServerSource: Option[Uri]) extends RequestSource {
    override def toInitial: RequestSourceInitial = RequestSourcePlugin(pluginName)

    override def getUri: Option[Uri] = appServerSource
  }



  /**
    * Plugin Request Source: AppServer / Plugin / Business Object
    */
  sealed trait RequestSourceInitial
  case object RequestSourceAppServer extends RequestSourceInitial
  case object RequestSourceAppServerDirect extends RequestSourceInitial
  case class RequestSourcePlugin(pluginName: String) extends RequestSourceInitial
  case class RequestSourceBusinessObject(boName: String) extends RequestSourceInitial

  case class `Plugin-Request-Source`(source: RequestSourceInitial) extends ModeledCustomHeader[`Plugin-Request-Source`] {
    override def companion: ModeledCustomHeaderCompanion[`Plugin-Request-Source`] = `Plugin-Request-Source`
    override def value(): String = source match {
      case RequestSourceAppServer => "AppServer"
      case RequestSourceAppServerDirect => "AppServerDirect"
      case RequestSourcePlugin(pluginName: String) => s"Plugin $pluginName"
      case RequestSourceBusinessObject(boName: String) => s"Business Object $boName"
    }
    override def renderInResponses(): Boolean = false
    override def renderInRequests(): Boolean = true
  }
  object `Plugin-Request-Source` extends ModeledCustomHeaderCompanion[`Plugin-Request-Source`] {
    val regex_plugin: Regex = "^Plugin (.+)$".r
    val regex_bo: Regex = "^Business Object (.+)$".r
    override def name: String = "Plugin-Request-Source"
    override def parse(value: String): Try[`Plugin-Request-Source`] = value match {
      case "AppServer" => Success(`Plugin-Request-Source`(RequestSourceAppServer))
      case "AppServerDirect" => Success(`Plugin-Request-Source`(RequestSourceAppServerDirect))
      case regex_plugin(pluginName) => Success(`Plugin-Request-Source`(RequestSourcePlugin(pluginName)))
      case regex_bo(boName) => Success(`Plugin-Request-Source`(RequestSourceBusinessObject(boName)))
      case other => Failure(new IllegalArgumentException(s"Invalid request source: $other"))
    }
  }

}
