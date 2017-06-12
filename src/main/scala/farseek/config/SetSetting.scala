package farseek.config

/** A [[Setting]] with a set of values.
  * @author delvr
  */
abstract class SetSetting[V](category: ConfigCategory, name: String, help: String, defaultValue: () => Set[V])
    extends Setting(category, name, help, defaultValue) {

    override def valueToString = value.map(writeElement).toSeq.sorted.mkString(", ")

    protected val valuesHelp = Seq("Comma-separated list of values")

    protected def parse(s: String) = s.split(",\\s+").flatMap(parseElement).toSet

    protected def parseElement(s: String): Option[V]

    protected def writeElement(e: V): String
}
