package farseek.world.gen

import farseek.core.ReplacedMethod
import net.minecraft.world._
import net.minecraft.world.chunk.IChunkProvider
import net.minecraftforge.fml.common.registry.GameRegistry

/** Method extensions related to chunk generation.
  * @author delvr
  */
object ChunkGeneratorExtensions {

    var populatingExtras = false

    /** Replacement method for [[GameRegistry.generateWorld()]] that sets [[populatingExtras]] to `true` during mod-provided chunk population. */
    def generateWorld(xChunk: Int, zChunk: Int, world: World, chunkGenerator: ChunkGenerator, chunkProvider: IChunkProvider,
                             super_generateWorld: ReplacedMethod[GameRegistry]) {
        populatingExtras = true
        super_generateWorld(xChunk, zChunk, world, chunkGenerator, chunkProvider)
        populatingExtras = false
    }
}
