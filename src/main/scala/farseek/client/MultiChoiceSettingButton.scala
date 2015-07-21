package farseek.client

import farseek.config._

/** A button that cycles through values of a [[MultiChoiceSetting]].
  * @author delvr
  */
class MultiChoiceSettingButton[T <: ChoiceValue[_]](setting: MultiChoiceSetting[_, T], index: Int,
                                                    _xPos: Int, _yPos: Int, _width: Int, _height: Int)
        extends ToggleSettingButton[T](setting, index, _xPos, _yPos, _width, _height) {

    protected def nextValue = setting.nextValue

}
