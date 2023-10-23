package io.simplifier.pluginapi.rest

import akka.http.scaladsl.model.Uri
import io.simplifier.pluginapi.rest.PluginHeaders._
import akka.http.scaladsl.client.RequestBuilding._

trait PluginRequestTransformers {

  /**
    * Add header "Simplifier-Call-Uri" to request.
    *
    * @note       when the uri is empty, then no header will be attached
    * @param uri  the uri
    * @return     request transformer
    */
  protected def addSimplifierCallUriHeader(uri: Option[Uri]): RequestTransformer = {
    uri match {
      case None => identity
      case Some(u) if u.isEmpty => identity
      case Some(u) =>
        val requiredUriString: String = s"${u.scheme}://${u.authority.toString()}"
        addHeader(`Simplifier-Call-Uri`(requiredUriString))
    }
  }

  /**
    * Add header "Plugin-Request-Source" to request.
    *
    * @param requestSource value of header
    * @return request transformer
    */
  protected def addRequestSourceHeader(requestSource: RequestSourceInitial): RequestTransformer = {
    addHeader(`Plugin-Request-Source`(requestSource))
  }

  /**
    * Add headers "Plugin-Request-Source" and optionally "Simplifier-Call-Uri" to request.
    */
  protected def addRequestSourceHeaders(requestSource: RequestSource): RequestTransformer = {
    addRequestSourceHeader(requestSource.toInitial) ~> addSimplifierCallUriHeader(requestSource.getUri)
  }

}