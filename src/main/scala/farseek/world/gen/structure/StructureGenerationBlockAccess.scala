package farseek.world.gen.structure

import farseek.util.XZ
import farseek.world.gen.InterruptedChunkGeneration
import farseek.world.{ChunkAccess, ServerBlockAccess}
import net.minecraft.world.WorldProvider
import net.minecraft.world.chunk.Chunk
import scala.collection.mutable

class StructureGenerationBlockAccess(provider: StructureGenerationChunkProvider) extends ServerBlockAccess with ChunkAccess {

    val worldProvider: WorldProvider = provider.world.provider

    private val loadedChunks = mutable.Map[XZ, Chunk]()

    def chunkAt(x: Int, z: Int) = {
        val xChunk = x >> 4
        val zChunk = z >> 4
        loadedChunks.getOrElseUpdate((xChunk, zChunk), generateChunk(xChunk, zChunk))
    }

    private def generateChunk(xChunk: Int, zChunk: Int): Chunk =
        try {
            provider.generator(xChunk, zChunk) // will throw InterruptedChunkGeneration
            throw new IllegalStateException(s"Illegal state in Farseek structure generation: $this")
        }
        catch { case InterruptedChunkGeneration(primer) => new Chunk(provider.world, primer, xChunk, zChunk) }
}
