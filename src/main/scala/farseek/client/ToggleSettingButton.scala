package farseek.client

import farseek.config._

/** A button that toggles or cycles through values of a [[Setting]].
  * @author delvr
  */
abstract class ToggleSettingButton[T](setting: Setting[T], index: Int, _xPosition: Int, _yPosition: Int, _width: Int, _height: Int)
        extends SettingButton(setting, index, _xPosition, _yPosition, _width, _height) {

    def clicked() { setValue(nextValue) }

    protected def nextValue: T
}
