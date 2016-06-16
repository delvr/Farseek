package farseek.util

import org.apache.logging.log4j.LogManager._
import org.apache.logging.log4j.Logger

/** Logging trait that wraps a [[Logger]] named after the first two dot-separated parts of the implementor's package name (ex.: `mod.farseek`).
  * Logging methods only resolve their argument to a String if the relevant logging level is enabled.
  *
  * @author delvr
  */
trait Logging {

  protected val logger = getLogger {
    val className = getClass.getName.split('.')
    if(className.length > 1) className(0) + '.' + className(1) else className(0)
  }

  def trace(message: => Any) {
    if(logger.isTraceEnabled)
      logger.trace(message)
  }

  def debug(message: => Any) {
    if(logger.isDebugEnabled)
      logger.debug(message)
  }

  def info(message: => Any) {
    if(logger.isInfoEnabled)
      logger.info(message)
  }

  def warn(message: => Any) {
    if(logger.isWarnEnabled)
      logger.warn(message)
  }

  def error(message: => Any) {
    if(logger.isErrorEnabled)
      logger.error(message)
  }
}
