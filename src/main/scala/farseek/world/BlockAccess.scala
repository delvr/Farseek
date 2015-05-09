package farseek.world

import cpw.mods.fml.relauncher.Side._
import cpw.mods.fml.relauncher.SideOnly
import farseek.util._
import net.minecraft.world._
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.util.ForgeDirection

/** An extension of [[IBlockAccess]] with some default implementations, and methods to access the [[WorldProvider]] and validate coordinates.
  * @author delvr
  */
trait BlockAccess extends IBlockAccess {

    def worldProvider: WorldProvider

    def validAt(xyz: XYZ): Boolean

    @deprecated(message = "Use validAt(XYZ)", since = "1.0.7")
    def validAt(xz: XZ): Boolean

    def isAirBlock(x: Int, y: Int, z: Int) = getBlock(x, y, z).isAir(this, x, y, z)

    def isSideSolid(x: Int, y: Int, z: Int, side: ForgeDirection, default: Boolean) = getBlock(x, y, z).isSideSolid(this, x, y, z, side)

    def isBlockProvidingPowerTo(x: Int, y: Int, z: Int, direction: Int) = getBlock(x, y, z).isProvidingStrongPower(this, x, y, z, direction)
}

/** A server-side [[BlockAccess]] where all client-only methods will throw an [[UnsupportedOperationException]].
  * @author delvr
  */
trait ServerBlockAccess extends BlockAccess {

    @SideOnly(CLIENT) def getHeight = unsupported
    @SideOnly(CLIENT) def extendedLevelsInChunkCache = unsupported
    @SideOnly(CLIENT) def getBiomeGenForCoords(x: Int, z: Int) = unsupported
    @SideOnly(CLIENT) def getLightBrightnessForSkyBlocks(x: Int, y: Int, z: Int, lightValue: Int) = unsupported
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

    def getBlock        (x: Int, y: Int, z: Int) = chunkAt(x, z).getBlock        (x & 15, y, z & 15)

    def getBlockMetadata(x: Int, y: Int, z: Int) = chunkAt(x, z).getBlockMetadata(x & 15, y, z & 15)

    def getTileEntity   (x: Int, y: Int, z: Int) = chunkAt(x, z).func_150806_e   (x & 15, y, z & 15)
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
