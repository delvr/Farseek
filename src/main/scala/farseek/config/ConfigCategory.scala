package farseek.config

import scala.collection._
import java.util.Properties
import java.io._

/** A [[ConfigElement]] setting with help text and load-time value parsing and defaulting.
  * @author delvr
  */
class ConfigCategory(category: Option[ConfigCategory], name: String) extends ConfigElement(category, name) {

    val elements = mutable.Buffer[ConfigElement]()

    val caption = s"$name Settings"

    private val file = new File("config", s"$id.properties")

    def load() {
        if(file.exists) {
            val props = new Properties
            props.load(new FileReader(file))
            load(props)
        }
    }

    def load(props: Properties) {
        elements.foreach(_.load(props))
    }

    def save() {
        val writer = new PrintWriter(new FileWriter(file))
        save(writer, fileColumns)
        writer.close()
    }

    def save(writer: PrintWriter, columns: Int) {
        writer.println( "#" * fileColumns)
        writer.println(s"# $caption")
        writer.println( "#" * fileColumns)
        writer.println()
        elements.foreach(_.save(writer, columns))
    }
}
