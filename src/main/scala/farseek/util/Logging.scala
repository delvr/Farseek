package farseek.util

import org.apache.logging.log4j.LogManager._
import org.apache.logging.log4j.Logger

/** Logging trait that wraps a Log4J [[Logger]] named by default after the first dot-separated part of the
  * implementing class's package name (ex.: `farseek`).
  * Logging methods only resolve their argument to a String if the relevant logging level is enabled.
  * @author delvr
  */
trait Logging {

    protected def loggerName = getClass.getName.split('.').head

    private val logger = getLogger(loggerName)

    def trace(message: => Any) {
        if(logger.isTraceEnabled) logger.trace(message)
    }

    def debug(message: => Any) {
        if(logger.isDebugEnabled) logger.debug(message)
    }

    def info(message: => Any) {
        if(logger.isInfoEnabled) logger.info(message)
    }

    def warn(message: => Any) {
        if(logger.isWarnEnabled) logger.warn(message)
    }

    def error(message: => Any) {
        if(logger.isErrorEnabled) logger.error(message)
    }
}
