package farseek

import com.bioxx.tfc.Blocks.Terrain.BlockCollapsible
import com.bioxx.tfc.Core.TFC_Climate._
import farseek.block.{BlockAndData, _}
import farseek.util.ImplicitConversions._
import farseek.util.{XYZ, _}
import farseek.world.biome._
import farseek.world.gen._
import farseek.world.{AbsoluteCoordinates, CoordinateSystem}
import net.minecraft.block.{BlockFalling, _}
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks._
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.WorldType._
import net.minecraft.world._
import net.minecraft.world.biome.BiomeGenBase
import net.minecraft.world.biome.BiomeGenBase._
import net.minecraft.world.chunk.Chunk
import scala.collection.SeqView

/** World and coordinates-related utility functions.
  *
  * Most of these functions take an implicit [[IBlockAccess]] parameter which can be a [[World]] or other block accessor,
  * and an implicit [[CoordinateSystem]] that specifies how to translate the provided X/Y/Z coordinates into absolute "world"
  * coordinates for the `IBlockAccess`. The default value is [[AbsoluteCoordinates]] for passing coordinates that are already absolute.
  * Different values can be provided when using relative/directed coordinates for structure components, for example.
  *
  * @author delvr
  */
package object world {

    val SurfaceDimensionId = 0
    val NetherDimensionId = -1
    val EndDimensionId = 1

    /** True while the game is generating or decorating chunks. */
    def populating = BlockFalling.fallInstantly || populatingExtras

    // ----------------------------------------------------------------------------------------------------------------
    // Y-ranges
    // ----------------------------------------------------------------------------------------------------------------

    /** Returns a view of all XYZ coordinates between yStart and yEnd, in reverse order if yStart > yEnd. */
    def posBetween(x: Int, z: Int, yStart: Int, yEnd: Int): SeqView[XYZ, Seq[_]] = between(yStart, yEnd).view.map((x, _, z))

    /** Returns a range of all Y coordinates between `y` and the top of `bac`. */
    def yUpFrom  (y: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Range = between(y, cs.yLocal(bac.yMax))

    /** Returns a range of all Y coordinates between `y` and the bottom of `bac`. */
    def yDownFrom(y: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Range = between(y, cs.yLocal(bac.yMin))

    /** Returns a view of all XYZ coordinates between `xyz` and the top of `bac`. */
    def upFrom(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): SeqView[XYZ, Seq[_]] =
        posBetween(xyz.x, xyz.z, xyz.y, cs.yLocal(bac.yMax))

    /** Returns a view of all XYZ coordinates between `xyz` and the bottom of `bac`. */
    def downFrom(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): SeqView[XYZ, Seq[_]] =
        posBetween(xyz.x, xyz.z, xyz.y, cs.yLocal(bac.yMin))

    /** Returns a view of all XYZ coordinates between `xyz` and the top of `bac` for which `f` returns `true`. */
    def takeUpFrom(xyz: XYZ, f: XYZ => Boolean)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): SeqView[XYZ, Seq[_]] =
        upFrom(xyz).takeWhile(f)

    /** Returns a view of all XYZ coordinates between `xyz` and the bottom of `bac` for which `f` returns `true`. */
    def takeDownFrom(xyz: XYZ, f: XYZ => Boolean)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): SeqView[XYZ, Seq[_]] =
        downFrom(xyz).takeWhile(f)

    /** Applies `g` to all XYZ coordinates between `xyz` and the top of `bac` for which `f` returns `true`. */
    def foreachUpFrom(xyz: XYZ, f: XYZ => Boolean, g: XYZ => Unit)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) {
        takeUpFrom(xyz, f).foreach(g)
    }

    /** Applies `g` to all XYZ coordinates between `xyz` and the bottom of `bac` for which `f` returns `true`. */
    def foreachDownFrom(xyz: XYZ, f: XYZ => Boolean, g: XYZ => Unit)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) {
        takeDownFrom(xyz, f).foreach(g)
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Getters
    // ----------------------------------------------------------------------------------------------------------------

    /** Returns the block at `xyz` if coordinates are valid and the block is not air. */
    def blockOptionAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Option[Block] =
        bac.xyzWorld(xyz).flatMap(xyz => blockOption(bac.getBlock(xyz.x, xyz.y, xyz.z)))

    def blockAt   (xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = blockOptionAt(xyz).getOrElse(air)
    def blockAbove(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = blockAt(above(xyz))
    def blockBelow(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = blockAt(below(xyz))

    /** Returns the block metadata at `xyz` if coordinates are valid. */
    def dataOptionAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Option[Int] =
        bac.xyzWorld(xyz).map(xyz => bac.getBlockMetadata(xyz.x, xyz.y, xyz.z))

    def dataAt   (xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = dataOptionAt(xyz).getOrElse(0)
    def dataAbove(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = dataAt(above(xyz))
    def dataBelow(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = dataAt(below(xyz))

    /** Returns the block and metadata at `xyz` if coordinates are valid and the block is not air. */
    def blockAndDataOptionAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Option[BlockAndData] =
        blockOptionAt(xyz).map(_ -> dataAt(xyz))

    def blockAndDataAt   (xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = blockAndDataOptionAt(xyz).getOrElse((air, 0))
    def blockAndDataAbove(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = blockAndDataAt(above(xyz))
    def blockAndDataBelow(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = blockAndDataAt(below(xyz))

    /** Returns the tile entity at `xyz` if coordinates are valid and a TileEntity is present. */
    def tileEntityOptionAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Option[TileEntity] =
        bac.xyzWorld(xyz).flatMap(xyz => Option(bac.getTileEntity(xyz.x, xyz.y, xyz.z)))

    def tileEntityAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = tileEntityOptionAt(xyz).get

    /** Returns the biome at `xyz` if coordinates are valid. The Y-coordinate is ignored in this implementation. */
    def biomeOptionAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): Option[BiomeGenBase] =
        bac.xyzWorld(xyz).map(xyz => blockAccessProvider(bac).getBiomeGenForCoords(xyz.x, xyz.z)) // NOT the client-only IBlockAccess.getBiomeGenForCoords()

    def biomeAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = biomeOptionAt(xyz).getOrElse(ocean)

    def baseBiomeAt(xyz: XYZ)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = biomeAt(xyz).base

    // ----------------------------------------------------------------------------------------------------------------
    // Setters
    // ----------------------------------------------------------------------------------------------------------------
    /** Sets `block` and `data` at `xyz` if coordinates are valid. */
    def setBlockAt[T <: IBlockAccess](xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true)
                                     (implicit bac: T, bw: BlockWriteAccess[T], cs: CoordinateSystem = AbsoluteCoordinates): Boolean =
        bac.xyzWorld(xyz).exists(bw.setBlockAt(_, block, data, notifyNeighbors))

    /** Sets `block` and `data` (as a tuple) at `xyz` if coordinates are valid. */
    def setBlockAndDataAt[T <: IBlockAccess](xyz: XYZ, blockAndData: BlockAndData, notifyNeighbors: Boolean = true)
                                            (implicit bac: T, bw: BlockWriteAccess[T], cs: CoordinateSystem = AbsoluteCoordinates) =
        setBlockAt(xyz, blockAndData.block, blockAndData.data, notifyNeighbors)

    /** Sets `block` at `xyz` to air if coordinates are valid. */
    def deleteBlockAt[T <: IBlockAccess](xyz: XYZ, notifyNeighbors: Boolean = true)
                                        (implicit bac: T, bw: BlockWriteAccess[T], cs: CoordinateSystem = AbsoluteCoordinates) =
        setBlockAt(xyz, air, 0, notifyNeighbors)

    /** Sets TileEntity `entity` at `xyz` if coordinates are valid. */
    def setTileEntityAt[T <: IBlockAccess](xyz: XYZ, entity: TileEntity)
                                          (implicit bac: T, bw: BlockWriteAccess[T], cs: CoordinateSystem = AbsoluteCoordinates): Boolean =
        bac.xyzWorld(xyz).exists(bw.setTileEntityAt(_, entity))

    // ----------------------------------------------------------------------------------------------------------------
    // Value class
    // ----------------------------------------------------------------------------------------------------------------

    /** Value class for [[IBlockAccess]] objects with utility methods. */
    implicit class IBlockAccessValue(val bac: IBlockAccess) extends AnyVal {

        def worldProvider: WorldProvider = bac match {
            case w: World => w.provider
            case c: ChunkCache => c.worldObj.provider
            case b: BlockAccess => b.worldProvider
        }

        private def chunkAt(x: Int, z: Int): Option[Chunk] = Option(bac match {
            case w: World       => w.getChunkFromBlockCoords(x, z)
            case c: ChunkCache  => if(c.isEmpty) null else c.chunkArray(c.chunkX + (x >> 4))(c.chunkZ + (z >> 4))
            case c: ChunkAccess => c.chunkAt(x, z)
            case _ => null
        })

        /** Returns absolute world coordinates for `xyzLocal` according to CoordinateSystem `cs`.
          * If `bac` is a [[BlockAccess]], the translated coordinates must satisfy [[BlockAccess.validAt()]] or None is returned. */
        def xyzWorld(xyzLocal: XYZ)(implicit cs: CoordinateSystem = AbsoluteCoordinates): Option[XYZ] = {
            val (xLocal, yLocal, zLocal) = xyzLocal
            val xyz = (cs.xWorld(xLocal, zLocal), cs.yWorld(yLocal), cs.zWorld(xLocal, zLocal))
            if(xyz.y >= yMin && xyz.y <= yMax && (bac match {
                case b: BlockAccess => b.validAt(xyz)
                case _ => true
            })) Some(xyz) else None
        }

        /** Returns the "actual height" of `bac` (the lower part of each chunk where terrain is generated). */
        def height = bac.getActualHeight

        def yMin = 0

        def yMax = height - 1

        /** Returns a Y-coordinate at or above the top non-air block at x/z. */
        def yTop(x: Int, z: Int) = chunkAt(x, z) match {
            case Some(chunk) => chunk.getTopFilledSegment + 15 // Optimization where possible
            case None => yMax
        }
    }

    /** Value class for [[WorldProvider]]s with utility methods. */
    implicit class WorldProviderValue(val provider: WorldProvider) extends AnyVal {
        def seaLevel: Option[Int] = if(provider.isSurfaceWorld && provider.getHorizon > 0d) Some(provider.getHorizon.toInt - 1) else None

        def lavaLevel: Option[Int] =
            if(provider.isSurfaceWorld && provider.terrainType != FLAT) Some(9)
            else if(provider.isHellWorld) Some(31)
            else None
    }

    // ----------------------------------------------------------------------------------------------------------------
    // World-specific
    // ----------------------------------------------------------------------------------------------------------------

    /** Returns true if water freezes at `xyz`. TFC-compatible. */
    def isFreezing(xyz: XYZ)(implicit w: World): Boolean = {
        val (x, y, z) = xyz
        if(tfcLoaded) getHeightAdjustedTemp(w, x, y, z) < 0f
        else biomeAt(xyz).getFloatTemperature(x, y, z) <= 0.15f &&
                w.getSavedLightValue(EnumSkyBlock.Block, x, y, z) <= 11 - blockAt(xyz).getLightOpacity
    }

    /** Returns true an entity of class `entityClass` is present inside the full-block bounding box at `xyz`. */
    def entityPresent(xyz: XYZ, entityClass: Class[_ <: Entity])(implicit w: World): Boolean = {
        val (x, y, z) = xyz
        w.getEntitiesWithinAABB(entityClass, stone.getCollisionBoundingBoxFromPool(w, x, y, z)).nonEmpty
    }

    /** Breaks block at `xyz` into its item (if any) and replaces it with air. */
    def break(xyz: XYZ)(implicit w: World): Boolean = displace(xyz, air)

    /** Breaks block at `xyz` into its item (if any) and replaces it with `block`. */
    def displace(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true)(implicit w: World): Boolean = {
        val (x, y, z) = xyz
        blockAt(xyz).dropBlockAsItem(w, x, y, z, dataAt(xyz), 0)
        setBlockAt(xyz, block, data, notifyNeighbors)
    }

    /** Returns true if a falling block at `xyz` should teleport to its landing position instead of turning into a falling entity.
      * This the case during chunk population and where any chunks in a 65-block cube centered on `xyz` are missing. */
    def blocksFallInstantlyAt(xyz: XYZ)(implicit w: World) = {
        val (x, y, z) = xyz
        BlockFalling.fallInstantly || (tfcLoaded && BlockCollapsible.fallInstantly) || !w.checkChunksExist(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32)
    }
}
