package farseek.config

/** A [[ConfigSetting]] with two or more unique [[ChoiceValue]]s.
  * @author delvr
  */
class MultiChoiceSetting[T, CT <: ChoiceValue[T]](name: String, help: String, defaultChoice: CT, choices: CT*)
        extends ConfigSetting(name, help, defaultChoice) {

    require(choices.size >= 2, "Need at least 2 values for setting $this")
    require(choices.map(_.toString.toLowerCase).toSet.size == choices.size, "Duplicate values for setting $this")

    protected def parse(s: String) = choices.find(_.toString.equalsIgnoreCase(s)).get

    protected val valuesHelp: Seq[String] = {
        ("Valid values: " + choices.map(v => if(v != defaultChoice) v else s"$v (default)").mkString(", ")) +:
            choices.collect { case cc: CustomChoice[_] => s"[${cc.name}]: ${cc.help}" }
    }

    def nextValue = choices((choices.indexOf(value) + 1) % choices.length)

    def matches(v: T) = value == v || value == Both || value == All
}

/** A value for a [[MultiChoiceSetting]].
  * @author delvr
  */
sealed abstract class ChoiceValue[T](name: String) {
    override val toString = name
}

/** A custom value for a [[MultiChoiceSetting]] with help text.
  * @author delvr
  */
case class CustomChoice[T](value: T, name: String, help: String) extends ChoiceValue(name)

/** A custom value for a [[MultiChoiceSetting]] without help text.
  * @author delvr
  */
case class SimpleCustomChoice[T](value: T, name: String) extends ChoiceValue(name)

/** Alternate [[SimpleCustomChoice]] constructor.
  * @author delvr
  */
object SimpleCustomChoice {
    def apply[T](value: T): SimpleCustomChoice[T] = SimpleCustomChoice(value, value.toString)
}

case object All extends ChoiceValue("All")
case object Non extends ChoiceValue("None")

case object Both     extends ChoiceValue("Both")
case object Neither  extends ChoiceValue("Neither")
