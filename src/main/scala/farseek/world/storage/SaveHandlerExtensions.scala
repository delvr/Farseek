package farseek.world.storage

import cpw.mods.fml.common._
import farseek.FarseekBaseMod
import farseek.core.ReplacedMethod
import farseek.util.ImplicitConversions._
import net.minecraft.nbt._
import net.minecraft.world.storage._

import scala.collection.mutable

/** Method patches related to loading and saving worlds.
  * @author delvr
  */
object SaveHandlerExtensions {

    /** Method patch for SaveHandler.readData that allows Farseek mods to warn about, or forbid, loading existing
      * worlds created without them. */
    def readData(handler: SaveHandler, worldInfo: WorldInfo, properties: java.util.Map[String, NBTBase],
                 tags: NBTTagCompound, super_readData: ReplacedMethod[FMLContainer])(implicit container: FMLContainer) {

        val savedModIds = mutable.Set[String]()
        val modTags = tags.getTagList("ModList", 10)
        for(i <- 0 until modTags.tagCount)
            savedModIds += modTags.getCompoundTagAt(i).getString("ModId")
        val loadedModContainers: mutable.Buffer[ModContainer] = Loader.instance.getModList

        val missingFarseekMods: Seq[FarseekBaseMod] = loadedModContainers.map(_.getMod).collect {
            case fm: FarseekBaseMod if !savedModIds.contains(fm.id) => fm }
        val missingMandatoryMods = missingFarseekMods.filter(_.requiresNewWorld)

        if(missingMandatoryMods.nonEmpty) {
            val missingModsList = missingMandatoryMods.map(_.name).mkString("\n")
            val errorMessage = s"The following mod(s) can only be used with new worlds:\n\n$missingModsList\n\n" +
                               "To load this save, remove the listed mod(s) and restart."
            StartupQuery.notify(errorMessage)
            StartupQuery.abort()
        } else {
            val missingWarningMods = missingFarseekMods.filter(_.existingWorldWarning.isDefined)
            if(missingWarningMods.nonEmpty) {
                val existingWorldWarnings = missingWarningMods.map(mod => mod.name + ": " + mod.existingWorldWarning.get)
                val warningsList = existingWorldWarnings.mkString("\n")
                val warningMessage = s"The following mod(s) can cause issues with existing worlds:\n\n$warningsList\n\n" +
                                     "It is recommended to make backups of your world before proceeding.\n\nContinue?"
                if(!StartupQuery.confirm(warningMessage)) StartupQuery.abort()
            } else
                super_readData(handler, worldInfo, properties, tags)
        }
    }
}
