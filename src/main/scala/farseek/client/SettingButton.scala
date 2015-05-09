package farseek.client

import farseek.config._
import net.minecraft.client.gui.GuiButton

/** A button for a [[Setting]].
  * @author delvr
  */
abstract class SettingButton[T](setting: Setting[T], index: Int, _xPosition: Int, _yPosition: Int, _width: Int, _height: Int)
        extends GuiButton(index, _xPosition, _yPosition, _width, _height, null) {

    setCaption()

    private def setCaption() { displayString = s"${setting.name}: $valueCaption" }

    protected def valueCaption = setting.valueToString

    protected def setValue(v: T) {
        setting.value = v
        setCaption()
    }

    def tooltip: Seq[String] = setting.help
}
