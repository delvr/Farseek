package farseek.config

import farseek.util.Logging
import java.io._
import java.util.Properties

/** A configuration setting or category with built-in help text and file save/load facilities. Part of a [[ConfigCategory]] unless it is the root category.
  * Predates Forge's own config GUI mechanism and can be used as a more functional alternative to it.
  * @author delvr
  */
abstract class ConfigElement(category: Option[ConfigCategory], val name: String) extends Logging {

    val id: String = category.map(_.id + '.').getOrElse("") + name.head.toLower + name.tail.replaceAll("\\W+", "")

    category.foreach(_.elements += this)

    protected val fileColumns = 120

    def load(props: Option[Properties])

    def save(writer: PrintWriter, columns: Int)

    override val toString = name
}
