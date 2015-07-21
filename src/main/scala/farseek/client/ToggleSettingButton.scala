package farseek.client

import farseek.config._

/** A button that toggles or cycles through values of a [[ConfigSetting]].
  * @author delvr
  */
abstract class ToggleSettingButton[T](setting: ConfigSetting[T], index: Int, _xPos: Int, _yPos: Int, _width: Int, _height: Int)
        extends SettingButton(setting, index, _xPos, _yPos, _width, _height) {

    def clicked() { setValue(nextValue) }

    protected def nextValue: T
}
