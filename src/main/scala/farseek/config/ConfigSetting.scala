package farseek.config

import farseek.util._
import java.io.PrintWriter
import scala.util.control.NonFatal

/** A [[ConfigElement]] setting with help text and load-time value parsing and defaulting.
  * @author delvr
  */
abstract class ConfigSetting[T](name: String, rawHelp: String, defaultValue: T) extends ConfigElement(name) {

    import farseek.config.ConfigElement._

    var value = defaultValue
    lazy val help = rawHelp +: valuesHelp

    private def propertyKey(parents: Seq[ConfigCategory]) =
        (parents :+ this).map(_.name.replaceAll("\\W+", "")).mkString(".")

    def load(parents: Seq[ConfigCategory], properties: Map[String, String]) {
        val key = propertyKey(parents)
        properties.get(key).foreach { valueString =>
            value = try parse(valueString) catch { case NonFatal(e) =>
                warn(s"Value $valueString is not valid for setting $key; substituting default value $defaultValue")
                defaultValue
            }
        }
    }

    def save(parents: Seq[ConfigCategory], writer: PrintWriter) = {
        val key = propertyKey(parents)
        wordWrap(help, fileColumns - commentPrefix.length).foreach(line => writer.println(commentPrefix + line))
        writer.println(s"$key=$valueToString")
        writer.println()
        Set(key)
    }

    protected def valuesHelp: Seq[String]

    protected def parse(s: String): T

    def valueToString = value.toString
}
