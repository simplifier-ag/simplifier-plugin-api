package io.simplifier.pluginapi

/**
  * Encapsulated performance logging data in user session.
  */
case class PerformanceLoggingData(jobName: Option[String], parentPerformanceId: Option[String],
                                  moduleName: Option[String], moduleInterfaceName: Option[String],
                                  clientBusinessObjectName: Option[String], clientBusinessObjectFunctionName: Option[String])

object PerformanceLoggingData {

  /**
    * Factory for performance logging data. If all given parameters are None, None will be returned.
    */
  def asOption(jobName: Option[String], parentPerformanceId: Option[String],
               moduleName: Option[String], moduleInterfaceName: Option[String],
               clientBusinessObjectName: Option[String], clientBusinessObjectFunctionName: Option[String]): Option[PerformanceLoggingData] = {
    (jobName, parentPerformanceId, moduleName, moduleInterfaceName, clientBusinessObjectName, clientBusinessObjectFunctionName) match {
      case (None, None, None, None, None, None) => None
      case _ => Some(PerformanceLoggingData(jobName, parentPerformanceId, moduleName, moduleInterfaceName, clientBusinessObjectName, clientBusinessObjectFunctionName))
    }
  }

}