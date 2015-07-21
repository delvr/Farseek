package farseek.client

import farseek.config._
import net.minecraft.client.gui.GuiButton

/** A [[GuiButton]] for a category of configuration options.
  * @author delvr
  */
class CategoryButton(val category: ConfigCategory, index: Int, _xPos: Int, _yPos: Int,
                     _width: Int, _height: Int, caption: String)
        extends GuiButton(index, _xPos, _yPos, _width, _height, caption)