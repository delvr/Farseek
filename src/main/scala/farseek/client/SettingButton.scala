package farseek.client

import farseek.config._
import net.minecraft.client.gui.GuiButton

/** A button for a [[ConfigSetting]].
  * @author delvr
  */
abstract class SettingButton[T](setting: ConfigSetting[T], index: Int, _xPos: Int, _yPos: Int, _width: Int, _height: Int)
        extends GuiButton(index, _xPos, _yPos, _width, _height, null) {

    setCaption()

    private def setCaption() { displayString = s"${setting.name}: $valueCaption" }

    protected def valueCaption = setting.valueToString

    protected def setValue(v: T) {
        setting.value = v
        setCaption()
    }

    def tooltip: Seq[String] = setting.help
}
