package farseek.config

import scala.math._

/** A [[Setting]] with Double values and a set precision and step.
  * @author delvr
  */
class NumericSetting(category: ConfigCategory, name: String, help: String, defaultValue: () => Double, min: Double, max: Double, step: Double = 1D)
        extends Setting(category, name, help, defaultValue) {

    require(max > min && step > 0 && step <= max - min, s"Invalid min/max/step/ for setting $this")

    private val precision = BigDecimal(step).scale - 1

    private val FormatString = s"%.${precision}f"

    override def valueToString = doubleToString(value)

    private def doubleToString(d: Double) = FormatString.format(d)

    protected def valuesHelp = Seq(s"Range: ${doubleToString(min)} to ${doubleToString(max)} (default: ${doubleToString(defaultValue())})")

    protected def parse(s: String) = {
        val f = s.toDouble
        require(f >= min && f <= max)
        f
    }

    def snapToStep(d: Double) = step * round(d / step).toDouble

    def normalized(d: Double) = (d - min) / (max - min)

    def denormalized(d: Double) = min + (max - min) * d
}
