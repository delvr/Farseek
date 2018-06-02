package farseek.world

import farseek.util.XYZ
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockAccess

/** A [[ServerBlockAccess]] that allows write access by setting blocks, metadata and tile entities.
  * @author delvr
  */
@deprecated("Use farseek.world.WorldOps instead", "2.3")
trait BlockSetter extends ServerBlockAccess {

    def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true): Boolean

    def setTileEntityAt(xyz: XYZ, entity: TileEntity): Boolean
}

/** A [[BlockSetter]] that wraps an [[IBlockAccess]] usable as a [[BlockWriteAccess]].
  * @author delvr
  */
@deprecated("Use farseek.world.WorldOps instead", "2.3")
trait WrappedBlockSetter[T <: IBlockAccess] extends WrappedBlockAccess with BlockSetter {

    implicit protected val wrapped: T

    protected val wrappedWriter: BlockWriteAccess[T]

    def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true) = wrappedWriter.setBlockAt(xyz, block, data, notifyNeighbors)

    def setTileEntityAt(xyz: XYZ, entity: TileEntity) = wrappedWriter.setTileEntityAt(xyz, entity)
}
