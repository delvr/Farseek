package farseek.world.gen

import farseek.core.ReplacedMethod
import farseek.world.gen.structure.StructureGenerationChunkProvider
import farseek.world.populating
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.{Chunk, IChunkProvider}

/** Method extensions related to chunk generation.
  * @author delvr
  */
object ChunkGeneratorExtensions {

  def provideChunk(xChunk: Int, zChunk: Int, super_provideChunk: ReplacedMethod[IChunkProvider])(implicit provider: IChunkProvider): Chunk = {
    populating = true
    val chunk: Chunk = super_provideChunk(xChunk, zChunk)
    chunk.worldObj match {
      case w: WorldServer if provider eq w.theChunkProviderServer.currentChunkProvider => StructureGenerationChunkProvider.onChunkProvided(chunk)
      case _ =>
    }
    populating = false
    chunk
  }
}
