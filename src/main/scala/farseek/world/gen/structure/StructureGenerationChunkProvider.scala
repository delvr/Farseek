package farseek.world.gen.structure

import com.pg85.otg.forge._
import com.pg85.otg.forge.generator.OTGChunkGenerator
import farseek.core._
import farseek.util.ImplicitConversions._
import farseek.util.Reflection._
import farseek.util._
import net.minecraft.world._
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.gen.structure.MapGenStructure
import net.minecraftforge.common.MinecraftForge._
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import scala.collection.mutable

/** A chunk provider for [[Structure]]s that contains a copy of a world's chunk generator and creates chunks _without_ structures.
  *
  * These chunks are used for structures being generated to query the terrain in their range without triggering recursion by generating more of themselves.
  * This enables Farseek structures to be terrain-aware in a way that vanilla structures cannot be, but also means that structures cannot
  * know of others that can appear in their range. The implementor is responsible for either preventing these situations (ex.: by partitioning
  * the world into distinct areas for generation, as is done for Streams) or handling collisions gracefully.
  *
  * @author delvr
  */
class StructureGenerationChunkProvider(world: WorldServer) extends Logging {

    val worldProvider = world.provider

    debug(s"Creating structure generation chunk provider for world $worldProvider")

    lazy val otgLoaded = classLoaded("com.pg85.otg.OTG")
    lazy val spongeLoaded = classLoaded("org.spongepowered.common.interfaces.world.IMixinWorldServer")
    lazy val spongeGeneratorMixin = classOf[WorldServer].getMethod("updateWorldGenerator")

    val generator: IChunkGenerator = {
      if(otgLoaded) {
        val otgEngine: Any = Class.forName("com.pg85.otg.OTG").getMethod("getEngine")(null)
        new OTGChunkGenerator(otgEngine.getClass.getMethod("getWorld", classOf[World])(otgEngine, world).asInstanceOf[ForgeWorld])
      } else if(spongeLoaded) {
        val mainGenerator = world.getChunkProvider.chunkGenerator
        spongeGeneratorMixin(world)
        val copyGenerator = world.getChunkProvider.chunkGenerator
        world.getChunkProvider.chunkGenerator = mainGenerator
        copyGenerator
      } else worldProvider.createChunkGenerator
    }

    classFieldValues[MapGenStructure](generator).foreach(_.range = -1) // Disable structure generators
    EVENT_BUS.register(this)

    def generateChunk(xChunk: Int, zChunk: Int) = {
        val chunk = generator.generateChunk(xChunk, zChunk)
        chunk.setTerrainPopulated(true)
        chunk
    }

    @SubscribeEvent def onWorldUnload(event: WorldEvent.Unload) {
        if(event.getWorld.provider == this.worldProvider) {
            debug(s"Removing structure generation chunk provider for world $worldProvider")
            EVENT_BUS.unregister(this)
            StructureGenerationChunkProvider.remove(event.getWorld.provider)
        }
    }
}

/** Companion object for [[StructureGenerationChunkProvider]]s, that maintains a mapping of [[WorldProvider]]s to `StructureGenerationChunkProvider`s.
  * @author delvr
  */
object StructureGenerationChunkProvider extends Logging {

    private val providers = mutable.Map[WorldProvider, StructureGenerationChunkProvider]()

    def apply(world: WorldServer) = providers.getOrElseUpdate(world, new StructureGenerationChunkProvider(world))

    def remove(worldProvider: WorldProvider) = providers.remove(worldProvider)
}
