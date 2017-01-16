package farseek.world

import farseek.FarseekBaseMod
import net.minecraft.nbt._
import net.minecraft.world.storage._
import net.minecraftforge.fml.common._
import scala.collection.JavaConversions._

package object storage {

  /** Method patch for SaveHandler.readData that allows Farseek mods to warn about loading existing worlds created without them. */
  def readData(container: WorldAccessContainer, handler: SaveHandler, worldInfo: WorldInfo,
               properties: java.util.Map[String, NBTBase], tags: NBTTagCompound): Unit = {
    val abort = container.isInstanceOf[FMLContainer] && {
      val modTags = tags.getTagList("ModList", 10)
      val savedModIds = (for(i <- 0 until modTags.tagCount) yield modTags.getCompoundTagAt(i).getString("ModId")).toSet
      val missingFarseekMods = Loader.instance.getModList.map(_.getMod).collect {
        case fm: FarseekBaseMod if !savedModIds.contains(fm.id) => fm
      }
      val missingModWarnings = missingFarseekMods.flatMap(mod =>
        mod.existingWorldWarning.map(warning => s"${mod.name}: $warning"))
      missingModWarnings.nonEmpty && !StartupQuery.confirm(Seq(
        "The following Farseek mod(s) can cause issues with existing worlds:",
         missingModWarnings.mkString("\n"),
        "It is recommended to make backups of your world before proceeding.",
        "Continue?").mkString("\n\n"))
    }
    if(abort) StartupQuery.abort() else container.readData(handler, worldInfo, properties, tags)
  }
}
