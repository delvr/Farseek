package farseek.world.gen

import farseek.util._
import farseek.world.biome.Biome
import farseek.world.gen.CoordinateSystem._
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity

/** A server-side [[Bounded]] block reader that validates all passed coordinates against its `bounds`.
  *
  * All public methods take implicit [[CoordinateSystem]] that specifies how to translate the provided X/Y/Z
  * coordinates into absolute "world" coordinates. The default value is [[AbsoluteCoordinates]] for passing
  * coordinates that are already absolute. Different values can be provided when using relative/directed coordinates
  * for structure components, for example.
  *
  * @throws IllegalArgumentException from any getter method that fails validation.
  * @author delvr
  */
trait BlockReader extends Bounds {

    def xzValidAt(xz: XZ)(implicit cs: CS = Abs) = contains(cs.xzWorld(xz))
    def xyzValidAt(xyz: XYZ)(implicit cs: CS = Abs) = contains(cs.xyzWorld(xyz))

    def height: Int = yMax - yMin + 1

    def randomSeed: Long

    /** Returns the lowest y-coordinate of `r` in local terms according to `cs`. */
    def yMinLocal(implicit cs: CS = Abs): Int = cs.yLocal(yMin)
    /** Returns the highest y-coordinate of `r` in local terms according to `cs`. */
    def yMaxLocal(implicit cs: CS = Abs): Int = cs.yLocal(yMax)

    def apply(xyz: XYZ)(implicit cs: CS = Abs): Block = blockAt(xyz)

    def blockAt(xyz: XYZ)(implicit cs: CS = Abs): Block = xyzValidated(xyz, blockAtValid)

    def dataAt(xyz: XYZ)(implicit cs: CS = Abs): Int = xyzValidated(xyz, dataAtValid)

    def entityAt(xyz: XYZ)(implicit cs: CS = Abs): Option[TileEntity] = xyzValidated(xyz, entityAtValid)

    def biomeAt(xz: XZ)(implicit cs: CS = Abs): Biome = xzValidated(xz, biomeAtValid)

    def topNonEmptyAt(xz: XZ)(implicit cs: CS = Abs): Option[XYZ] = xzValidated(xz, topNonEmptyAtValid)

    protected def blockAtValid(xyz: XYZ): Block

    protected def dataAtValid(xyz: XYZ): Int

    protected def entityAtValid(xyz: XYZ): Option[TileEntity]
    
    protected def biomeAtValid(xz: XZ): Biome

    protected def topNonEmptyAtValid(xz: XZ): Option[XYZ]

    private def xyzValidated[T](xyz: XYZ, f: XYZ => T)(implicit cs: CS = Abs): T = {
        val wxyz = cs.xyzWorld(xyz)
        require(xyzValidAt(wxyz)(Abs), s"Local coordinates $xyz (world coordinates $wxyz) not valid for $this")
        f(wxyz)
    }

    private def xzValidated[T](xz: XZ, f: XZ => T)(implicit cs: CS = Abs): T = {
        val wxz = cs.xzWorld(xz)
        require(xzValidAt(wxz)(Abs), s"Local coordinates $xz (world coordinates $wxz) not valid for $this")
        f(wxz)
    }
}

trait ChunkSet {
    def xzChunks: Set[XZ]
}

/** A [[BlockReader]] that wraps another `BlockReader` and delegates all methods except `bounds` to it.
  * This reader's `bounds` are the intersection of `outerBounds` and `r.bounds`.
  * @author delvr
  */
class BlockReaderWrapper(protected val r: BlockReader, val outerBounds: Bounded, val randomSeed: Long)
        extends BlockReader {

    val bounds = r.intersectionWith(outerBounds)

    protected def blockAtValid(xyz: XYZ) = r.blockAt(xyz)

    protected def dataAtValid(xyz: XYZ) = r.dataAt(xyz)

    protected def entityAtValid(xyz: XYZ) = r.entityAt(xyz)

    protected def biomeAtValid(xz: XZ) = r.biomeAt(xz)

    protected def topNonEmptyAtValid(xz: XZ) = r.topNonEmptyAt(xz)
}
