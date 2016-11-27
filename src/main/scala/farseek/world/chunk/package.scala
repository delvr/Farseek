package farseek.world

import farseek.world.gen.structure.StructureGenerationChunkProvider
import net.minecraft.world.chunk._
import net.minecraft.world.gen.ChunkProviderServer

package object chunk {

  def provideChunk(provider: IChunkProvider, xChunk: Int, zChunk: Int): Chunk = {
    populating = true
    val chunk = provider.provideChunk(xChunk, zChunk)
    if(provider.isInstanceOf[ChunkProviderServer]) StructureGenerationChunkProvider.onChunkProvided(chunk)
    populating = false
    chunk
  }
}
