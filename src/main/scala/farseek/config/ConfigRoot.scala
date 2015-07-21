package farseek.config

import farseek.FarseekBaseMod
import java.io._
import java.util.Properties
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * @author delvr
 */
class ConfigRoot(mod: FarseekBaseMod) extends ConfigCategory(mod.name) {

    import farseek.config.ConfigElement._

    private val file = new File("config", s"$name.properties")

    def load() {
        if(file.exists) {
            val properties = new Properties
            properties.load(new FileReader(file))
            load(Seq.empty, properties.toMap)
        }
    }

    def save() {
        val filePropertyLines =
            if(file.exists) Source.fromFile(file).getLines().filter(line =>
                !line.trim.startsWith(commentSymbol) && line.contains('=')).toSeq // toSeq allows closing the file
            else Seq.empty
        val writer = new PrintWriter(new FileWriter(file))
        val savedPropertyKeys = save(Seq.empty, writer)
        val missingProperyLines = filePropertyLines.filterNot(line =>
            savedPropertyKeys.exists(key => line.startsWith(s"$key=")))
        if(missingProperyLines.nonEmpty) {
            printHeader(writer, "Unknown Settings (for objects not yet loaded, or unused in latest mod combination)")
            missingProperyLines.foreach(writer.println)
        }
        writer.close()
    }
}
