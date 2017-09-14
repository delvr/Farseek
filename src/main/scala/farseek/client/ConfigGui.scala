package farseek.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory

/** An [[IModGuiFactory]] implementation that defines a mod's [[RootConfigScreen]].
  * @author delvr
  */
class ConfigGui extends IModGuiFactory {

  val hasConfigGui = true
  val runtimeGuiCategories = null

  def createConfigGui(parentScreen: GuiScreen) = new RootConfigScreen(parentScreen)

  def initialize(minecraft: Minecraft) {}
}
