package io.simplifier.pluginapi

import scala.annotation.implicitNotFound

@implicitNotFound("You are missing an implicit UserSession in the scope. Forward from REST calls or Plugin Slots")
case class UserSession(tokenOpt: Option[String], userIdOpt: Option[Long], appNameOpt: Option[String],
                       performanceLoggingData: Option[PerformanceLoggingData], isInternalUser: Boolean = false)

object UserSession {
  /** Unauthenticated user session */
  def unauthenticated: UserSession = UserSession(None, None, None, None)
}