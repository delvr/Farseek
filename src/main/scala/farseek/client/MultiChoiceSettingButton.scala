package farseek.client

import farseek.config._

/** A button that cycles through values of a [[MultiChoiceSetting]].
  * @author delvr
  */
class MultiChoiceSettingButton(setting: MultiChoiceSetting, index: Int, _xPosition: Int, _yPosition: Int, _width: Int, _height: Int)
        extends ToggleSettingButton(setting, index, _xPosition, _yPosition, _width, _height) {

    protected def nextValue = setting.nextValue

}
