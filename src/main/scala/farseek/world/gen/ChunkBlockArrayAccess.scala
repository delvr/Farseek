package farseek.world.gen

import farseek.util.ImplicitConversions._
import farseek.util._
import farseek.world._
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.WorldProvider
import net.minecraft.world.chunk.ChunkPrimer

/** A [[BlockSetter]] backed by an array of blocks and an array of metadata for a chunk.
  * It is meant for use during chunk generation by a [[net.minecraft.world.gen.IChunkGenerator]] or other generator implementation.
  * @author delvr
  */
class ChunkBlockArrayAccess(val worldProvider: WorldProvider, xChunk: Int, zChunk: Int, primer: ChunkPrimer)
        extends BlockSetter with BoundedBlockAccess {

    val boundingBox = sizedBox(xChunk*ChunkSize, 0, zChunk*ChunkSize, ChunkSize, 256, ChunkSize)

    def getBlock(x: Int, y: Int, z: Int) = primer.getBlockState(x & 15, y, z & 15)

    def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true) = {
        primer.setBlockState(xyz.x & 15, xyz.y, xyz.z & 15, (block, data))
        true
    }

    def getBlockMetadata(x: Int, y: Int, z: Int) = primer.getBlockState(x & 15, y, z & 15)

    def getTileEntity(x: Int, y: Int, z: Int) = unsupported

    def setTileEntityAt(xyz: XYZ, entity: TileEntity) = unsupported
}
