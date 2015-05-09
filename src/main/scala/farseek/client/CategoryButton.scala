package farseek.client

import farseek.config._
import net.minecraft.client.gui.GuiButton

/** A [[GuiButton]] for a category of configuration options.
  * @author delvr
  */
class CategoryButton(val category: ConfigCategory, index: Int, _xPosition: Int, _yPosition: Int, _width: Int, _height: Int, caption: String)
        extends GuiButton(index, _xPosition, _yPosition, _width, _height, caption)