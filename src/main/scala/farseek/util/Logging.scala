package farseek.util

import org.apache.logging.log4j.LogManager._
import org.apache.logging.log4j._

/** Logging trait that wraps a [[Logger]] named "net.minecraft" so it logs at INFO level in logs/latest.log when using the default Forge log4j2 config.
  * Log messages are prefixed by the first dot-separated part of the implementor's package name.
  * Logging methods only resolve their argument to a String if the relevant logging level is enabled.
  *
  * @author delvr
  */
trait Logging {

  private val logger = getLogger("net.minecraft")

  private val prefix = s"[${getClass.getName.takeWhile(_ != '.')}] "

  def trace(message: => Any): Unit = if(logger.isTraceEnabled) logger.trace(prefix + message)

  def debug(message: => Any): Unit = if(logger.isDebugEnabled) logger.debug(prefix + message)

  def info (message: => Any): Unit = if(logger.isInfoEnabled)  logger.info(prefix + message)

  def warn (message: => Any): Unit = if(logger.isWarnEnabled)  logger.warn(prefix + message)

  def error(message: => Any): Unit = if(logger.isErrorEnabled) logger.error(prefix + message)
}
