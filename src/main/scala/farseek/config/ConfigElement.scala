package farseek.config

import farseek.util.Logging
import java.io._

/** A configuration setting or category with built-in help text and file save/load facilities.
  * @author delvr
  */
abstract class ConfigElement(val name: String) extends Logging {

    def load(parents: Seq[ConfigCategory], properties: Map[String, String])

    def save(parents: Seq[ConfigCategory], writer: PrintWriter): Set[String]

    override def equals(that: Any) = that match {
        case c: ConfigElement => c.name == this.name
        case _ => false
    }

    override def toString = name
}

object ConfigElement {
    val fileColumns = 120
    val commentSymbol = "#"
    val commentPrefix = commentSymbol + " "
    val separator = commentSymbol * fileColumns

    def printHeader(writer: PrintWriter, caption: String) {
        writer.println(separator)
        writer.println(s"# $caption")
        writer.println(separator)
        writer.println()
    }
}
