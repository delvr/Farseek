package farseek.client

import cpw.mods.fml.client.GuiModList
import cpw.mods.fml.common.ModContainer
import farseek.FarseekBaseMod
import farseek.util.Reflection._
import net.minecraft.client.gui.GuiScreen

/** The root configuration screen for a mod's top-level configuration category.
  * @author delvr
  */
class RootConfigScreen(parent: GuiScreen) extends ConfigScreen(parent) {

    protected lazy val category = classFieldValues[ModContainer](
        parent.asInstanceOf[GuiModList]).head.getMod.asInstanceOf[FarseekBaseMod].configuration.get

    /** Saves the configuration on exit. */
    override protected def close() {
        category.save()
        super.close()
    }
}
