package farseek.client

import net.minecraft.client.Minecraft
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.IModGuiFactory.RuntimeOptionCategoryElement

/** An [[IModGuiFactory]] implementation that defines a mod's [[RootConfigScreen]].
  * @author delvr
  */
class ConfigGui extends IModGuiFactory {

    def initialize(minecraft: Minecraft) {}

    def mainConfigGuiClass = classOf[RootConfigScreen]

    def runtimeGuiCategories = null

    def getHandlerFor(element: RuntimeOptionCategoryElement) = null
}

