package farseek.world

import farseek.util.ImplicitConversions._
import farseek.util._
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.util.math.BlockPos
import net.minecraft.world._
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.relauncher.Side._
import net.minecraftforge.fml.relauncher.SideOnly

/** An extension of [[IBlockAccess]] with some default implementations, and methods to access the [[WorldProvider]] and validate coordinates.
  * @author delvr
  */
trait BlockAccess extends IBlockAccess {

    def worldProvider: WorldProvider

    def validAt(xyz: XYZ): Boolean

    @deprecated(message = "Use validAt(XYZ)", since = "1.0.7")
    def validAt(xz: XZ): Boolean

    def isAirBlock(pos: BlockPos) = {
      val state = getBlockState(pos)
        state.getBlock.isAir(state, this, pos)
    }

    def isSideSolid(pos: BlockPos, side: EnumFacing, default: Boolean) = getBlockState(pos).isSideSolid(this, pos, side)

    def getStrongPower(pos: BlockPos, direction: EnumFacing) = {
      val state = getBlockState(pos)
      state.getBlock.getStrongPower(state, this, pos, direction)
    }

    def getBlockState(pos: BlockPos) = getBlock(pos.getX, pos.getY, pos.getZ)

    def getTileEntity(pos: BlockPos) = getTileEntity(pos.getX, pos.getY, pos.getZ)

    def getBlock(x: Int, y: Int, z: Int): Block

    def getBlockMetadata(x: Int, y: Int, z: Int): Int

    def getTileEntity(x: Int, y: Int, z: Int): TileEntity
}

/** A server-side [[BlockAccess]] where all client-only methods will throw an [[UnsupportedOperationException]].
  * @author delvr
  */
trait ServerBlockAccess extends BlockAccess {

    @SideOnly(CLIENT) def getWorldType = unsupported
    @SideOnly(CLIENT) def extendedLevelsInChunkCache = unsupported
    @SideOnly(CLIENT) def getBiomeGenForCoords(pos: BlockPos) = unsupported
    @SideOnly(CLIENT) def getCombinedLight(pos: BlockPos, lightValue: Int) = unsupported
}

/** A [[BlockAccess]] where getters are delegated to the chunk returned by `chunkAt()`.
  * @author delvr
  */
trait ChunkAccess extends BlockAccess {

    /** Returns true if between 0 and `worldProvider.getActualHeight`. */
    def validAt(xyz: XYZ) = xyz.y >= 0 && xyz.y < worldProvider.getActualHeight

    @deprecated(message = "Use validAt(XYZ)", since = "1.0.7")
    def validAt(xz: XZ) = true

    def chunkAt(x: Int, z: Int): Chunk

    def getBlock        (x: Int, y: Int, z: Int) = chunkAt(x, z).getBlockState(x, y, z)

    def getBlockMetadata(x: Int, y: Int, z: Int) = chunkAt(x, z).getBlockState(x, y, z)

    def getTileEntity   (x: Int, y: Int, z: Int) = chunkAt(x, z).getTileEntity((x, y, z), Chunk.EnumCreateEntityType.IMMEDIATE)
}

/** A [[BlockAccess]] that wraps another `BlockAccess` and delegates all methods except `validAt()` to it.
  * @author delvr
  */
trait WrappedBlockAccess extends BlockAccess {

    protected val wrapped: IBlockAccess

    def worldProvider = wrapped.worldProvider

    def getBlock(x: Int, y: Int, z: Int) = wrapped.getBlock(x, y, z)

    def getBlockMetadata(x: Int, y: Int, z: Int) = wrapped.getBlockMetadata(x, y, z)

    def getTileEntity(x: Int, y: Int, z: Int) = wrapped.getTileEntity(x, y, z)
}
