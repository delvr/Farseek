package farseek.util

import java.util.logging.Logger._

/** Logging trait that wraps a [[java.util.logging.Logger]] named after the first two dot-separated parts of the implementor's package name (ex.: `mod.farseek`).
  * Logging methods only resolve their argument to a String if the relevant logging level is enabled.
  * @author delvr
  */
trait Logging {

    private val logger = getLogger {
        val className = getClass.getName.split('.')
        if(className.length > 1) className(0) + '.' + className(1) else className(0)
    }

    def trace(message: => Any) {
        logger.finest(message.toString)
    }

    def debug(message: => Any) {
        logger.fine(message.toString)
    }

    def info(message: => Any) {
        logger.info(message.toString)
    }

    def warn(message: => Any) {
        logger.warning(message.toString)
    }

    def error(message: => Any) {
        logger.severe(message.toString)
    }
}
