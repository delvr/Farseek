package farseek.client

import farseek.config._
import net.minecraft.client.resources.I18n

/** A button that toggles the on/off value of a [[BooleanSetting]].
  * @author delvr
  */
class OnOffSettingButton(setting: BooleanSetting, index: Int, _xPosition: Int, _yPosition: Int, _width: Int, _height: Int)
        extends ToggleSettingButton(setting, index, _xPosition, _yPosition, _width, _height) {

    protected def nextValue = !setting.value

    override protected def valueCaption = I18n.format("options." + (if(setting.value) "on" else "off"))

}
