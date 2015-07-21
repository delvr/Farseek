package farseek

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import farseek.block._
import farseek.util._
import net.minecraft.block.Block
import net.minecraft.block.Block._
import net.minecraft.block.material.Material

/** Non-core mod class for Farseek. `@Mod` annotation parameters for mod and dependencies versions should be replaced
  * by the build process.
  * @see [[farseek.core.FarseekCoreMod]] for core mod class.
  * @author delvr
  */
@Mod(modLanguage = "scala", modid = "farseek", version = "SNAPSHOT",
    dependencies = "required-after:Forge;after:terrafirmacraft")
object FarseekMod extends FarseekBaseMod {

    val name = "Farseek"
    val description = "A Scala modding API based on Forge."
    val authors = Seq("delvr")

    val configuration = None

    /** Prints shader config message.
      * Runs late so other mods get to register blocks, etc. that could be referenced here. */
    @EventHandler def handle(event: FMLLoadCompleteEvent) {
        val (vanillaWaterBlocks, customWaterBlocks) = materialBlocks(Material.water).partition(isVanillaClass)
        val (vanillaLavaBlocks, customLavaBlocks) = materialBlocks(Material.lava).partition(isVanillaClass)
        val (vanillaIceBlocks, customIceBlocks) = materialBlocks(Material.ice).partition(isVanillaClass)
        if(customWaterBlocks.nonEmpty || customLavaBlocks.nonEmpty || customIceBlocks.nonEmpty) {
            printout("Shader configuration: " +
                     "to use shaders with modded liquid or ice blocks, edit your shader's configuration files like so:")
            printout()
            if(customWaterBlocks.nonEmpty)
                printShaderConfigMessageEntry("water", vanillaWaterBlocks, customWaterBlocks)
            if(customIceBlocks.nonEmpty)
                printShaderConfigMessageEntry("water", vanillaIceBlocks, customIceBlocks)
            if(customLavaBlocks.nonEmpty)
                printShaderConfigMessageEntry("terrain", vanillaLavaBlocks, customLavaBlocks)
            printout("Note that these IDs will change with every new combination of loaded mods.")
        }
    }

    private def printShaderConfigMessageEntry(fileSuffix: String, vanillaBlocks: Set[Block], customBlocks: Set[Block]) {
        printout(s"In gbuffers_$fileSuffix.wsh file, replace the line:")
        printout(shaderConfigLine(vanillaBlocks))
        printout("with the following line:")
        printout(shaderConfigLine(vanillaBlocks ++ customBlocks))
        printout()
    }

    private def shaderConfigLine(blocks: Set[Block]) =
        "    if (" + blocks.map(getIdFromBlock).toSeq.sorted.map("mc_Entity.x == " + _).mkString( " || ") + " {"
 }
