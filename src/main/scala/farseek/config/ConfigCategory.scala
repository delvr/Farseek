package farseek.config

import java.io._
import scala.collection.mutable

/** A [[ConfigElement]] setting with help text and load-time value parsing and defaulting.
  * @author delvr
  */
class ConfigCategory private(name: String, parent: Option[ConfigCategory]) extends ConfigElement(name) {

    import farseek.config.ConfigElement._

    def this(name: String, parent: ConfigCategory) = this(name, Some(parent))

    protected def this(name: String) = this(name, None)

    val elements = mutable.LinkedHashSet[ConfigElement]() // Unique, but preserving insertion order

    parent.foreach(_ += this)

    def += (element: ConfigElement) { elements += element }

    def load(parents: Seq[ConfigCategory], properties: Map[String, String]) {
        elements.foreach(_.load(parents :+ this, properties))
    }

    def save(parents: Seq[ConfigCategory], writer: PrintWriter) = {
        if(parents.size == 1) {
            printHeader(writer, s"$name Settings")
            writer.println()
        }
        elements.flatMap(_.save(parents :+ this, writer)).toSet
    }
}
