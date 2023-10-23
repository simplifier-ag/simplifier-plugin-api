package io.simplifier.pluginapi.rest

import akka.http.scaladsl.model.StatusCodes.Forbidden
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives.{complete, failWith, headerValueByType, optionalHeaderValueByType, pass, provide, reject}
import akka.http.scaladsl.server.{Directive0, Directive1, MalformedHeaderRejection}
import io.simplifier.pluginapi.helper.PluginLogger
import io.simplifier.pluginapi.{PerformanceLoggingData, UserSession}
import io.simplifier.pluginapi.rest.PluginHeaders._

import scala.util.Try

/**
 * Akka-Http directives for the [[PluginHeaders]].
 */
object PluginDirectives extends PluginLogger {

  /**
   * Directive to verify the correct plugin secret was provided.
   *
   * @param correctSecret secret to check
   * @return rejection, if the secret was invalid, pass otherwise
   */
  def verifyPluginSecret(correctSecret: String): Directive0 = {
    headerValueByType(`Plugin-Secret`) flatMap { hdr =>
      if (correctSecret == hdr.secret) {
        pass
      } else {
        complete(Forbidden -> "Invalid plugin secret"): Directive0
      }
    }
  }

  /**
   * Extract (optional) user id from request header.
   *
   * @return directive providing the userId option
   */
  private def extractUserId: Directive1[Option[Long]] = {
    optionalHeaderValueByType(`Simplifier-User`) map (_.map(_.userId))
  }

  /**
   * Extract (optional) session token from request header.
   *
   * @return directive providing the token option
   */
  private def extractSimplifierToken: Directive1[Option[String]] = {
    optionalHeaderValueByType(SimplifierToken) map (_.map(_.token))
  }

  private def extractAppName: Directive1[Option[String]] = {
    optionalHeaderValueByType(AppName) map (_.map(_.appName))
  }

  private def extractJobName: Directive1[Option[String]] = {
    optionalHeaderValueByType(JobName) map (_.map(_.jobName))
  }

  private def extractParentPerformanceId: Directive1[Option[String]] = {
    optionalHeaderValueByType(ParentPerformanceId) map (_.map(_.parentPerformanceId))
  }

  private def extractModuleName: Directive1[Option[String]] = {
    optionalHeaderValueByType(ModuleName) map (_.map(_.moduleName))
  }

  private def extractModuleInterfaceName: Directive1[Option[String]] = {
    optionalHeaderValueByType(ModuleInterfaceName) map (_.map(_.moduleInterfaceName))
  }

  private def extractClientBusinessObject: Directive1[Option[String]] = {
    optionalHeaderValueByType(ClientBusinessObjectName) map (_.map(_.clientBusinessObjectName))
  }

  private def extractClientBusinessObjectFunction: Directive1[Option[String]] = {
    optionalHeaderValueByType(ClientBusinessObjectFunctionName) map (_.map(_.clientBusinessObjectFunctionName))
  }

  private def extractIsInternalUser: Directive1[Option[Boolean]] = {
    optionalHeaderValueByType(IsInternalUser) map (_.map(_.isInternalUser))
  }

  /**
   * Extract (optional) Simplifier call URI from request header.
   *
   * @return directive providing the Simplifier call URI
   */
  def extractSimplifierCallUri: Directive1[Option[String]] = {
    optionalHeaderValueByType(`Simplifier-Call-Uri`) map (_.map(_.uri))
  }


  /**
   * Extract user session from request header.
   *
   * @return directive providing the [[UserSession]]
   */
  def extractUserSession: Directive1[UserSession] = {
    extractSimplifierToken flatMap { tokenOpt =>
      extractUserId flatMap { userIdOpt =>
        extractAppName flatMap { appNameOpt =>
          extractJobName flatMap { jobNameOpt =>
            extractParentPerformanceId flatMap { parentPerformanceId =>
              extractModuleName flatMap { moduleNameOpt =>
                extractModuleInterfaceName flatMap { moduleInterfaceNameOpt =>
                  extractClientBusinessObject flatMap { clientBusinessObjectOpt =>
                    extractClientBusinessObjectFunction flatMap { clientBusinessObjectFunctionOpt =>
                      extractIsInternalUser flatMap { isInternalUser =>
                        provide {
                          UserSession(tokenOpt, userIdOpt, appNameOpt, PerformanceLoggingData.asOption(
                            jobNameOpt, parentPerformanceId, moduleNameOpt, moduleInterfaceNameOpt, clientBusinessObjectOpt, clientBusinessObjectFunctionOpt
                          ), isInternalUser.getOrElse(false))
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Extract request source from header.
   *
   * @return directive providing request source
   */
  def extractRequestSource: Directive1[RequestSource] = {
    headerValueByType(`Plugin-Request-Source`) flatMap {
      case `Plugin-Request-Source`(requestSource) => provide(requestSource)
    } flatMap (rs => {
      extractSimplifierCallUri flatMap { initialSource =>
        (initialSource, rs) match {
          case (None, RequestSourceAppServerDirect) => provide(AppServerDirect(None))
          case (Some(is), RequestSourceAppServerDirect) => Try(Uri(is))
            .fold(e => failWith(new IllegalArgumentException(s"The provided Uri: {$is} cannot be parsed", e)), uri => provide(AppServerDirect(Some(uri))))
          case (None, RequestSourceAppServer) => log.warn("A request relayed from the Simplifier requires the original source header!")
            provide(AppServer(None))
          case (Some(is), RequestSourceAppServer) => Try(Uri(is))
            .fold(e => failWith(new IllegalArgumentException(s"The provided Uri: {$is} cannot be parsed", e)), uri => provide(AppServer(Some(uri))))
          case (None, RequestSourcePlugin(name)) => provide(Plugin(name, None))
          case (Some(is), RequestSourcePlugin(name)) => Try(Uri(is))
            .fold(e => failWith(new IllegalArgumentException(s"The provided Uri: {$is} cannot be parsed", e)), uri => provide(Plugin(name, Some(uri))))
          case (None, RequestSourceBusinessObject(name)) => log.warn("A request relayed from a Business Object requires the original source header!")
            provide(BusinessObject(name, None))
          case (Some(is), RequestSourceBusinessObject(name)) => provide(BusinessObject(name, Some(is)))
        }
      }
    })
  }

  /**
   * Extract plugin request source from header. All other request sources
   * (appserver, business object) will result in a rejection.
   *
   * @return directive providing plugin request sources
   */
  def extractRequestSourcePlugin: Directive1[Plugin] = {
    headerValueByType(`Plugin-Request-Source`) flatMap {
      case `Plugin-Request-Source`(pluginSource: RequestSourcePlugin) =>
        provide(pluginSource) flatMap (ps => {
          extractSimplifierCallUri flatMap {
            case None => provide(Plugin(ps.pluginName, None))
            case is@Some(source) => Try(Uri(source))
              .fold(e => failWith(new IllegalArgumentException(s"The provided Uri: {$is} cannot be parsed", e)), uri => provide(Plugin(ps.pluginName, Some(uri))))
          }
        })
      case hdr@`Plugin-Request-Source`(_) => reject(MalformedHeaderRejection(hdr.name, s"Unsupported request source: {${hdr.value()}}"))
    }
  }

}
