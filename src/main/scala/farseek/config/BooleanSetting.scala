package farseek.config

/** A [[ConfigSetting]] with a Boolean value.
  * @author delvr
  */
class BooleanSetting(name: String, help: String, defaultValue: Boolean) extends ConfigSetting(name, help, defaultValue) {

    protected def parse(value: String) = value.toBoolean

    protected val valuesHelp = Nil

}
