package farseek.world.gen

import farseek.util._
import farseek.world.BlockWriteAccess.WorldBlockAccess
import farseek.world._
import net.minecraft.world._

/** A [[WrappedBlockSetter]] around a [[World]] that defines a chunk-sized [[BoundingBox]] in the middle of a 4-chunk intersection, and
  * restricts reading and writing to those 4 chunks (a 32x32 area). Used when populating newly-generated chunks.
  * @author delvr
  */
class PopulatingArea(xChunk: Int, zChunk: Int, implicit val wrapped: WorldServer) extends WrappedBlockSetter[World] with Bounded {

    protected val wrappedWriter = WorldBlockAccess

    val boundingBox = sizedBox(xChunk*ChunkSize + ChunkSize/2, zChunk*ChunkSize + ChunkSize/2, ChunkSize)

    val paddedBox = sizedBox(xChunk*ChunkSize, zChunk*ChunkSize, ChunkSize*2)

    def validAt(xyz: XYZ) = paddedBox.contains(xyz)

    @deprecated(message = "Use validAt(XYZ)", since = "1.0.7")
    def validAt(xz: XZ) = paddedBox.contains(xz)
}
