package farseek

import farseek.config.ConfigCategory
import farseek.util.Logging
import net.minecraftforge.fml.common._
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event._

/** Convenience base class for Farseek mods. Provides configuration and metadata support.
  * @author delvr
  */
abstract class FarseekBaseMod extends Logging {

    private lazy val container = Loader.instance.getReversedModObjectList.get(this)
    lazy val id   = container.getModId
    lazy val name = container.getName

    def configuration: Option[ConfigCategory]

    /** If defined, worlds created without this mod can be loaded but only after the user confirms the onscreen warning. */
    def existingWorldWarning: Option[String] = None

    /** Loads and saves configuration file if defined, creating it if non-existent.
      * Runs late so other mods get to register blocks, etc. that could be referenced in configuration values. */
    @EventHandler def handle(event: FMLLoadCompleteEvent) {
        configuration.foreach { config =>
            config.load()
            config.save()
        }
    }
}

object FarseekBaseMod {
  final val GuiFactory = "farseek.client.ConfigGui"
}
