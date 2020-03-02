package farseek.world.gen.structure

import farseek.util.XZ
import farseek.world.{ChunkAccess, ServerBlockAccess}
import net.minecraft.world.chunk.Chunk
import scala.collection.mutable

class StructureGenerationBlockAccess(provider: StructureGenerationChunkProvider) extends ServerBlockAccess with ChunkAccess {

    val worldProvider = provider.worldProvider

    private val loadedChunks = mutable.Map[XZ, Chunk]()

    def chunkAt(x: Int, z: Int) = {
        val xChunk = x >> 4
        val zChunk = z >> 4
        loadedChunks.getOrElseUpdate((xChunk, zChunk), provider.generateChunk(xChunk, zChunk))
    }
}
