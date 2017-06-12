package farseek.config

import farseek.util._
import java.io.PrintWriter
import java.util.Properties
import scala.util.control.NonFatal

/** A [[ConfigElement]] setting with help text and load-time value parsing and defaulting.
  * @author delvr
  */
abstract class Setting[T](category: ConfigCategory, name: String, rawHelp: String, defaultValue: () => T) extends ConfigElement(Some(category), name) {

    private val commentPrefix = "# "

    var value: T = _
    lazy val help = rawHelp +: valuesHelp

    def load(properties: Option[Properties]) = properties match {
        case Some(props) =>
            val prop = props.getProperty(id)
            value = try parse(prop) catch { case NonFatal(_) =>
                warn(s"Value $prop is not valid for setting $id; substituting default value $defaultValue")
                defaultValue()
            }
        case None => value = defaultValue()
    }

    def save(writer: PrintWriter, columns: Int) {
        wordWrap(help, columns - commentPrefix.length).foreach(line => writer.println(commentPrefix + line))
        writer.println(s"$id=$valueToString")
        writer.println()
    }

    protected def valuesHelp: Seq[String]

    protected def parse(s: String): T

    def valueToString = value.toString
}
