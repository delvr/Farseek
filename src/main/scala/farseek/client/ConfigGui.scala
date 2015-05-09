package farseek.client

import cpw.mods.fml.client.IModGuiFactory._
import cpw.mods.fml.client._
import net.minecraft.client.Minecraft

/** An [[IModGuiFactory]] implementation that defines a mod's [[RootConfigScreen]].
  * @author delvr
  */
class ConfigGui extends IModGuiFactory {

    def initialize(minecraft: Minecraft) {}

    def mainConfigGuiClass = classOf[RootConfigScreen]

    def runtimeGuiCategories = null

    def getHandlerFor(element: RuntimeOptionCategoryElement) = null
}

