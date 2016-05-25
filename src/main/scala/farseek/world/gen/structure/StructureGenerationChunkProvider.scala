package farseek.world.gen.structure

import farseek.util.Reflection._
import farseek.util._
import farseek.world._
import net.minecraft.world._
import net.minecraft.world.chunk._
import net.minecraft.world.gen.structure.MapGenStructure
import net.minecraftforge.common.MinecraftForge._
import net.minecraftforge.event.terraingen.PopulateChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import scala.collection.mutable

/** A chunk provider for [[Structure]]s that contains a copy of a world's chunk generator and creates chunks _without_ structures.
  *
  * These chunks are used for structures being generated to query the terrain in their range without triggering recursion by generating more of themselves.
  * This enables Farseek structures to be terrain-aware in a way that vanilla structures cannot be, but also means that structures cannot
  * know of others that can appear in their range. The implementor is responsible for either preventing these situations (ex.: by partitionning
  * the world into distinct areas for generation, as is done for Streams) or handling collisions gracefully.
  *
  * @author delvr
  */
class StructureGenerationChunkProvider(val worldProvider: WorldProvider) extends ServerBlockAccess with ChunkAccess with Logging {

    debug(s"Creating structure generation chunk provider for world $worldProvider")

    val generator = worldProvider.createChunkGenerator
    private val loadedChunks = mutable.Map[XZ, Chunk]()

    classFieldValues[MapGenStructure](generator).foreach(_.range = -1) // Disable structure generators
    EVENT_BUS.register(this)

    def chunkAt(x: Int, z: Int) = {
        val xChunk = x >> 4
        val zChunk = z >> 4
        loadedChunks.getOrElseUpdate((xChunk, zChunk), generateChunk(xChunk, zChunk))
    }

    private def generateChunk(xChunk: Int, zChunk: Int) = {
        val chunk = generator.provideChunk(xChunk, zChunk)
        chunk.setTerrainPopulated(true)
        chunk
    }

    // "real" chunk is ready with all structures in range, so we don't go there again
    // Note that since we don't recreate structures on world load, reloaded chunks can be "generated through" and won't be unloaded, but this should remain a small number
    @SubscribeEvent def onPrePopulateChunk(event: PopulateChunkEvent.Pre) {
        if(event.getWorld.provider == this.worldProvider)
            loadedChunks.remove(event.getChunkX, event.getChunkZ)
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

    def apply(worldProvider: WorldProvider) = providers.getOrElseUpdate(worldProvider, new StructureGenerationChunkProvider(worldProvider))

    def remove(worldProvider: WorldProvider) = providers.remove(worldProvider)
}
