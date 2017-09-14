package farseek.config

/** A [[Setting]] with two or more unique [[ChoiceValue]]s.
  * @author delvr
  */
class MultiChoiceSetting(category: ConfigCategory, name: String, help: String, defaultValue: () => ChoiceValue, values: ChoiceValue*)
        extends Setting(category, name, help, defaultValue) {

    require(values.size >= 2, "Need at least 2 values for setting $this")
    require(values.map(_.toString.toLowerCase).toSet.size == values.size, "Duplicate values for setting $this")

    protected def parse(s: String) = values.find(_.toString.equalsIgnoreCase(s)).get

    protected lazy val valuesHelp =
        ("Valid values: " + values.map(v => v + (if(v == defaultValue()) " (default)" else "")).mkString(", ") + '.') +:
         values.collect { case CustomChoice(n, h) => s"[$n]: ${h()}" }

    def nextValue = values((values.indexOf(value) + 1) % values.length)

    def matches(v: ChoiceValue) = value == v || value == Both || value == All
}

/** A value for a [[MultiChoiceSetting]].
  * @author delvr
  */
sealed abstract class ChoiceValue(name: String) {
    override val toString = name
}

case class CustomChoice(name: String, help: () => String) extends ChoiceValue(name)

case object Non  extends ChoiceValue("None")
case object Both extends ChoiceValue("Both")
case object All  extends ChoiceValue("All")
