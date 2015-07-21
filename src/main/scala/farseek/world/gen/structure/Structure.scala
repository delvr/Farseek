package farseek.world.gen.structure

import farseek.util._
import farseek.world.gen._
import net.minecraft.world.gen.structure._

/** Farseek implementation of world generation structures, as an alternative to vanilla [[StructureStart]]s.
  * @author delvr
  */
abstract class Structure extends Bounds with Logging {

    final def generateIn(r: BlockReader, randomSeed: Long): Boolean =
        generate(new BlockReaderWrapper(r, bounds, randomSeed))

    final def carveIn(w: BlockWriter with ChunkSet) {
        carve(new BlockWriterWrapper(w, bounds, w.randomSeed, w.xzChunks))
    }

    final def buildIn(w: BlockWriter with ChunkSet) {
        build(new BlockWriterWrapper(w, bounds, w.randomSeed, w.xzChunks))
    }

    protected def generate(implicit r: BlockReader): Boolean

    protected def carve(implicit w: BlockWriter)

    protected def build(implicit w: BlockWriter)
}
