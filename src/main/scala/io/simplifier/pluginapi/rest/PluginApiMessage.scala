package io.simplifier.pluginapi.rest

import akka.http.scaladsl.marshalling.PredefinedToEntityMarshallers.stringMarshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.stringUnmarshaller
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.{read, write}

/**
  * Base trait for Plugin-based API messages.
  */
trait PluginApiMessage

/**
  * Plugin-based API messages marshalling/unmarshalling.
  */
object PluginApiMessage {

  implicit val formats = DefaultFormats

  implicit def toEntityMarshaller[A <: PluginApiMessage]: ToEntityMarshaller[A] =
    stringMarshaller(`application/json`).compose(write(_))

  implicit def fromEntityUnmarshaller[A <: PluginApiMessage : Manifest]: FromEntityUnmarshaller[A] =
    stringUnmarshaller.forContentTypes(`application/json`).map(read[A])

}