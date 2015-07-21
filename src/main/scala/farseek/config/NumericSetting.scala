package farseek.config

import farseek.util._
import scala.math._

/** A [[ConfigSetting]] with Double values and a set precision and step.
  * @author delvr
  */
class NumericSetting(name: String, help: String, defaultValue: Double, min: Double, max: Double, step: Double = 1D)
        extends ConfigSetting(name, help, defaultValue) {

    require(max > min && step > 0 && step <= max - min && defaultValue >= min && defaultValue <= max,
            s"Invalid min/max/step/default for setting $this")

    private val precision = BigDecimal(step).scale - 1

    private val FormatString = s"%.${precision}f"

    override def valueToString = doubleToString(value)

    private def doubleToString(d: Double) = FormatString.format(d)

    protected val valuesHelp =
        Seq(s"Range: ${doubleToString(min)} to ${doubleToString(max)}" +
            textIf(step != 1D, s" by increments of ${doubleToString(step)}") +
            s" (default: ${doubleToString (defaultValue)})")

    protected def parse(s: String) = {
        val f = s.toDouble
        require(f >= min && f <= max)
        f
    }

    def snapToStep(d: Double) = step * round(d / step).toDouble

    def normalized(d: Double) = (d - min) / (max - min)

    def denormalized(d: Double) = min + (max - min) * d
}
