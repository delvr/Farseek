package farseek.config

/** A [[Setting]] with a Boolean value.
  * @author delvr
  */
class BooleanSetting(category: ConfigCategory, name: String, help: String, defaultValue: Boolean)
        extends Setting(category, name, help, defaultValue) {

    protected def parse(value: String) = value.toBoolean

    protected val valuesHelp = Nil

}
