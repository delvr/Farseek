package farseek.util

import org.apache.logging.log4j.LogManager._
import org.apache.logging.log4j.Logger

/** Logging trait that wraps a [[Logger]] named "farseek." followed by the first dot-separated part of the implementor's package name.
  * Logging methods only resolve their argument to a String if the relevant logging level is enabled.
  *
  * @author delvr
  */
trait Logging {

  private val logger = getLogger("farseek." + getClass.getName.takeWhile(_ != '.'))

  def trace(message: => Any): Unit = if(logger.isTraceEnabled) logger.trace(message)

  def debug(message: => Any): Unit = if(logger.isDebugEnabled) logger.debug(message)

  def info (message: => Any): Unit = if(logger.isInfoEnabled)  logger.info(message)

  def warn (message: => Any): Unit = if(logger.isWarnEnabled)  logger.warn(message)

  def error(message: => Any): Unit = if(logger.isErrorEnabled) logger.error(message)
}
