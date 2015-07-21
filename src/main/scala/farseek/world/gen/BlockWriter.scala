package farseek.world.gen

import farseek.block._
import farseek.util._
import farseek.world.biome._
import farseek.world.gen.CoordinateSystem._
import farseek.world.{WorldData, _}
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.world._

/** A [[BlockReader]] that provides write access.
  * @author delvr
  */
trait BlockWriter extends BlockReader {

    def update(xyz: XYZ, block: Block)(implicit cs: CS = Abs) {
        val wxyz = cs.xyzWorld(xyz)
        if(xyzValidAt(wxyz)(Abs)) setBlockAtValid(wxyz, block)
    }

    def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, localize: Boolean = true,
                   notifyNeighbors: Boolean = true)(implicit cs: CS = Abs) {
        val wxyz = cs.xyzWorld(xyz)
        if(xyzValidAt(wxyz)(Abs)) setBlockAtValid(wxyz, block, data, localize, notifyNeighbors)
    }

    def setBlockAndDataAt(xyz: XYZ, blockAndData: BlockAndData)(implicit cs: CS = Abs) {
        val wxyz = cs.xyzWorld(xyz)
        if(xyzValidAt(wxyz)(Abs)) setBlockAtValid(wxyz, blockAndData.block, blockAndData.data)
    }

    def setEntityAt(xyz: XYZ, entity: TileEntity)(implicit cs: CS = Abs) {
        val wxyz = cs.xyzWorld(xyz)
        if(xyzValidAt(wxyz)(Abs)) setEntityAtValid(wxyz, entity)
    }

    def setBiomeAt(xz: XZ, biome: Biome)(implicit cs: CS = Abs) {
        val wxz = cs.xzWorld(xz)
        if(xzValidAt(wxz)(Abs)) setBiomeAtValid(wxz, biome)
    }

    protected def setBlockAtValid(xyz: XYZ, block: Block, data: Int = 0,
                                  localize: Boolean = true, notifyNeighbors: Boolean = true)

    protected def setEntityAtValid(xyz: XYZ, entity: TileEntity)

    protected def setBiomeAtValid(xz: XZ, biome: Biome)
}

/** A [[BlockWriter]] that wraps another `BlockWriter` and delegates all methods except `bounds` to it.
  * @author delvr
  */
class BlockWriterWrapper(w: BlockWriter, outerBounds: Bounded, randomSeed: Long, val xzChunks: Set[XZ])
        extends BlockReaderWrapper(w, outerBounds, randomSeed) with BlockWriter with ChunkSet {

    protected def setBlockAtValid(xyz: XYZ, block: Block, data: Int = 0, localize: Boolean, notifyNeighbors: Boolean) {
        w.setBlockAt(xyz, block, data, localize, notifyNeighbors)
    }

    protected def setEntityAtValid(xyz: XYZ, entity: TileEntity) { w.setEntityAt(xyz, entity) }

    protected def setBiomeAtValid(xz: XZ, biome: Biome) { w.setBiomeAt(xz, biome) }
}

/** A [[BlockWriter]] for a [[World]]. To avoid unnecessary instantiations, it is accessible only through the cached
  * instances in the companion object.
  * @author delvr
  */
class WorldBlockWriter private(w: World) extends BlockWriter {

    val bounds = BoundingBox(-30000000, w.yMin, -30000000, 29999999, w.yMax, 29999999)

    val randomSeed = w.getSeed

    protected def blockAtValid(xyz: XYZ) = w.blockAt(xyz)

    protected def dataAtValid(xyz: XYZ) = w.dataAt(xyz)

    protected def entityAtValid(xyz: XYZ) = w.entityAt(xyz)

    protected def biomeAtValid(xz: XZ) = w.biomeAt(xz)

    protected def topNonEmptyAtValid(xz: XZ) = w.topNonEmptyAt(xz)

    protected def setBlockAtValid(xyz: XYZ, block: Block, data: Int, localize: Boolean, notifyNeighbors: Boolean) {
        w.setBlockAt(xyz, block, data, breakPrevious = false, localize, notifyNeighbors)
    }

    protected def setEntityAtValid(xyz: XYZ, entity: TileEntity) { w(xyz) = entity }

    protected def setBiomeAtValid(xz: XZ, biome: Biome) { w(xz) = biome }
}

object WorldBlockWriter extends WorldData[WorldBlockWriter] {
    protected def newData(world: World) = new WorldBlockWriter(world)
}