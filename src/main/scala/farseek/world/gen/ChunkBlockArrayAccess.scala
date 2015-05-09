package farseek.world.gen

import farseek.util._
import farseek.world._
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.WorldProvider

/** A [[BlockSetter]] backed by an array of blocks and an array of metadata for a chunk.
  * It is meant for use during chunk generation by a [[net.minecraft.world.gen.ChunkProviderGenerate]] or other generator implementation.
  * @author delvr
  */
class ChunkBlockArrayAccess(val worldProvider: WorldProvider, xChunk: Int, zChunk: Int, blocks: Array[Block], datas: Option[Array[Byte]] = None, val yBottom: Int = 0)
        extends BlockSetter with BoundedBlockAccess {

    private val height = blocks.length / ChunkArea

    val boundingBox = sizedBox(xChunk*ChunkSize, yBottom, zChunk*ChunkSize, ChunkSize, height, ChunkSize)

    private def index(x: Int, y: Int, z: Int) = ((x & 15)*16 + (z & 15))*height - yBottom + y

    def getBlock(x: Int, y: Int, z: Int) = blocks(index(x, y, z))

    def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true) = {
        val i = index(xyz.x, xyz.y, xyz.z)
        blocks(i) = block
        datas.foreach(_(i) = (data & 255).toByte)
        true
    }

    def getBlockMetadata(x: Int, y: Int, z: Int) = datas match {
        case Some(d) => d(index(x, y, z))
        case None => 0
    }

    def getTileEntity(x: Int, y: Int, z: Int) = unsupported

    def setTileEntityAt(xyz: XYZ, entity: TileEntity) = unsupported
}