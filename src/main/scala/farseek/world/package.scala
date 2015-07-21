package farseek

import com.bioxx.tfc.Core.TFC_Climate._
import com.bioxx.tfc.Core.TFC_Core._
import com.bioxx.tfc.WorldGen.DataLayer
import farseek.block.{BlockAndData, _}
import farseek.util.ImplicitConversions._
import farseek.util.{XYZ, _}
import farseek.world.biome._
import net.minecraft.block._
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks._
import net.minecraft.tileentity.TileEntity
import net.minecraft.world._
import net.minecraft.world.biome._
import net.minecraft.world.chunk.Chunk

/** World and coordinates-related utility functions.
  * @author delvr
  */
package object world {

    val ChunkSize = 16
    val ChunkHeight = 256
    val ChunkArea = ChunkSize * ChunkSize
    val ChunkVolume = ChunkArea * ChunkHeight
    val iChunkMax = ChunkSize - 1

    def iChunk(iWorld: Int) = iWorld >> 4
    def xzChunk(xzWorld: XZ) = (iChunk(xzWorld.x), iChunk(xzWorld.z))

    def iWorld(iChunk: Int) = iChunk << 4
    def xzWorld(xzChunk: XZ) = (iWorld(xzChunk.x), iWorld(xzChunk.z))

    def iInChunk(i: Int): Int = i & iChunkMax
    def xzInChunk(xz: XZ): XZ = (iInChunk(xz.x), iInChunk(xz.z))
    def xyzInChunk(xyz: XYZ): XYZ = (iInChunk(xyz.x), xyz.y, iInChunk(xyz.z))

    /** Value class for [[IBlockAccess]] objects with utility methods. */
    implicit class IBlockAccessValue(val r: IBlockAccess) extends AnyVal {

        def yMin: Int = 0

        def yMax: Int = height - 1

        def height: Int = world.getActualHeight

        def apply(xyz: XYZ): Block = blockAt(xyz)

        def blockAt(xyz: XYZ): Block = r.getBlock(xyz.x, xyz.y, xyz.z)

        def dataAt(xyz: XYZ): Int = r.getBlockMetadata(xyz.x, xyz.y, xyz.z)
        
        def blockAndDataAt(xyz: XYZ): BlockAndData = (blockAt(xyz), dataAt(xyz))

        def entityAt(xyz: XYZ): Option[TileEntity] = Option(r.getTileEntity(xyz.x, xyz.y, xyz.z)).filter(!_.isInvalid)

        def biomeAt(xz: XZ): Biome = r match {
            case wo: World => wo.getBiomeGenForCoordsBody(xz.x, xz.z)
            case cc: ChunkCache => chunkAt(xz) match {
                case Some(chunk) => chunk.getBiomeGenForWorldCoords(xz.x, xz.z, world.getWorldChunkManager)
                case None => world.getWorldChunkManager.getBiomeGenAt(xz.x, xz.z)
            }
        }

        def topNonEmptyAt(xz: XZ): Option[XYZ] = {
            val yStart = chunkAt(xz) match {
                case Some(chunk) => chunk.getTopFilledSegment + 15
                case None => yMax
            }
            xz(yStart).to(yMin).find(!isEmptyAt(_))
        }

        def isEmptyAt(xyz: XYZ): Boolean = r.isAirBlock(xyz.x, xyz.y, xyz.z)

        protected def world = r match {
            case wo: World => wo
            case cc: ChunkCache => cc.worldObj
        }

        protected def chunkAt(xz: XZ): Option[Chunk] = r match {
            case wo: World => Option(wo.getChunkFromBlockCoords(xz.x, xz.z))
            case cc: ChunkCache if cc.isEmpty => None
            case cc: ChunkCache =>
                val xIndex = iChunk(xz.x) - cc.chunkX
                val zIndex = iChunk(xz.z) - cc.chunkZ
                if(cc.chunkArray.hasIndex(xIndex) && cc.chunkArray(xIndex).hasIndex(zIndex))
                    Option(cc.chunkArray(xIndex)(zIndex))
                else None
        }
    }

    /** Value class for [[World]]s with utility methods. */
    implicit class WorldValue(val w: World) extends AnyVal {

        def update(xyz: XYZ, block: Block) { setBlockAt(xyz, block) }

        def update(xyz: XYZ, blockAndData: BlockAndData) { setBlockAt(xyz, blockAndData, blockAndData) }

        def update(xyz: XYZ, entity: TileEntity) { w.setTileEntity(xyz.x, xyz.y, xyz.z, entity) }

        def update(xz: XZ, biome: Biome) {
            w.getChunkFromBlockCoords(xz.x, xz.z).getBiomeArray()(xzInChunk(xz).zxFlatArrayIndex(ChunkSize)) =
                    unsignedByte(biome.biomeID)
        }

        def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, breakPrevious: Boolean = false,
                       localize: Boolean = true, notifyNeighbors: Boolean = true) {
            val (x, y, z) = xyz
            if(breakPrevious)
                w(xyz).dropBlockAsItem(w, x, y, z, w.dataAt(xyz), 0)
            val (localBlock, localData) =
                if(localize) localizedBlockAndDataAt(xyz, block, data) else (block, data)
            w.setBlock(x, y, z, localBlock, localData, if(notifyNeighbors) 3 else 2)
        }

        def displaceBlockAt(xyz: XYZ, block: Block, data: Int = 0,
                            localize: Boolean = true, notifyNeighbors: Boolean = true) {
            setBlockAt(xyz, block, data, breakPrevious = true, localize = localize, notifyNeighbors = notifyNeighbors)
        }

        def breakBlockAt(xyz: XYZ, data: Int = 0, localize: Boolean = true, notifyNeighbors: Boolean = true) {
            displaceBlockAt(xyz, air, data, localize = localize, notifyNeighbors = notifyNeighbors)
        }

        /** Returns true if water freezes at `xyz`. TFC-compatible. */
        def waterFreezesAt(xyz: XYZ): Boolean = {
            val (x, y, z) = xyz
            if(tfcLoaded) getHeightAdjustedTemp(w, x, y, z) < 0f
            else w.biomeAt(xyz).getFloatTemperature(x, y, z) <= 0.15f &&
                 w.getSavedLightValue(EnumSkyBlock.Block, x, y, z) <= 11 - w(xyz).getLightOpacity
        }

        /** Returns true an entity of class `entityClass` is present inside the full-block bounding box at `xyz`. */
        def entityPresentAt(xyz: XYZ, entityClass: Class[_ <: Entity]): Boolean = {
            val (x, y, z) = xyz
            w.getEntitiesWithinAABB(entityClass, stone.getCollisionBoundingBoxFromPool(w, x, y, z)).nonEmpty
        }

        def localizedBlockAndDataAt(xyz: XYZ, block: Block, data: Int = 0): BlockAndData = block match {
            case `grass`  =>  grassBlockFor(xyz)
            case `dirt`   =>   dirtBlockFor(xyz)
            case `sand`   =>   sandBlockFor(xyz)
            case `gravel` => gravelBlockFor(xyz)
            case `stone`  =>   rockBlockFor(xyz)
            case _ => (block, data)
        }

        /** Returns the vanilla grass block, or the appropriate TFC grass block if TFC is loaded. */
        def grassBlockFor(xyz: XYZ): BlockAndData =
            if(tfcLoaded) tfcGroundBlockAndDataAt(xyz, layerData =>
                getTypeForGrassWithRain(layerData, getRainfall(w, xyz.x, xyz.y, xyz.z)))
            else grass

        /** Returns the vanilla dirt block, or the appropriate TFC dirt block if TFC is loaded. */
        def dirtBlockFor(xyz: XYZ): BlockAndData =
            if(tfcLoaded) tfcGroundBlockAndDataAt(xyz, getTypeForDirt)
            else dirt

        /** Returns the vanilla biome-specific sand block, or the appropriate TFC sand block if TFC is loaded. */
        def sandBlockFor(xyz: XYZ): BlockAndData =
            if(tfcLoaded) tfcGroundBlockAndDataAt(xyz, getTypeForSand)
            else {
                val biome = w.biomeAt(xyz).base
                if(biome.topBlock.isSand) biome.topBlockAndData
                else sand
            }

        /** Returns the vanilla gravel block, or the appropriate TFC gravel block if TFC is loaded. */
        def gravelBlockFor(xyz: XYZ): BlockAndData =
            if(tfcLoaded) tfcGroundBlockAndDataAt(xyz, getTypeForGravel)
            else gravel

        /** Returns the vanilla biome-specific rock block (always stone if the block above is stone),
          * or the appropriate TFC rock block if TFC is loaded. */
        def rockBlockFor(xyz: XYZ): BlockAndData = {
            if(w(xyz.above) == stone)
                stone
            else w.biomeAt(xyz).base match {
                case nether: BiomeGenHell => netherrack
                case end: BiomeGenEnd => end_stone
                case tfcBiome if tfcLoaded =>
                    val layer = tfcLayerAt(xyz)
                    (layer.block, layer.data2)
                case biome =>
                    val top = biome.topBlockAndData
                    if(top == yellowSand) sandstone
                    else if(top == redSand) (stained_hardened_clay, Orange)
                    else stone
            }
        }

        private def tfcLayerAt(xyz: XYZ): DataLayer = {
            val (x, y, z) = xyz
            getRockLayer(w, x, y, z, getRockLayerFromHeight(w, x, y, z))
        }

        private def tfcGroundBlockAndDataAt(xyz: XYZ, f: Int => Block): BlockAndData = {
            val layer = tfcLayerAt(xyz)
            (f(layer.data1), getSoilMeta(layer.data1))
        }
    }
}
