package farseek.world.gen

import farseek.util._
import farseek.world._
import farseek.world.biome._
import farseek.util.BoundingBox._
import net.minecraft.block.Block
import net.minecraft.init.Blocks._
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
@author delvr */
class ChunkGenerationData(world: World, xzChunk: XZ, yMin: Int, blocks: Array[Block],
                          datas: Option[Array[Byte]] = None, biomes: Option[Array[Biome]] = None)
        extends BlockReader with ChunkSet {

    val bounds = sizedBox(xzChunk.x*ChunkSize, yMin, xzChunk.z*ChunkSize, ChunkSize, blocks.length / ChunkArea, ChunkSize)

    val xzChunks = Set(xzChunk)

    val randomSeed = chunkRandomSeed(xzChunk, world.getSeed)

    protected def blockAtValid(xyz: XYZ) = Option(blocks(index(xyz))).getOrElse(air)

    protected def dataAtValid(xyz: XYZ) = datas.map(_(index(xyz)).toInt).getOrElse(0)

    protected def entityAtValid(xyz: XYZ) = None

    protected def biomeAtValid(xz: XZ) = biomes.map(_(index(xz))).getOrElse(world.getWorldChunkManager.getBiomeGenAt(xz.x, xz.z))

    protected def topNonEmptyAtValid(xz: XZ) = xz(yMax).to(yMin).find(blockAtValid(_) != air)

    protected def index(xyz: XYZ) = (xyzInChunk(xyz) - yMin).xzyFlatArrayIndex(ChunkSize, height)

    protected def index(xz: XZ) = xzInChunk(xz).zxFlatArrayIndex(ChunkSize)
}

class ChunkGenerationWritableData(world: World, xzChunk: XZ, yMin: Int, blocks: Array[Block],
                                  datas: Option[Array[Byte]] = None, biomes: Option[Array[Biome]] = None)
        extends ChunkGenerationData(world, xzChunk, yMin, blocks, datas, biomes) with BlockWriter {

    protected def setBlockAtValid(xyz: XYZ, block: Block, data: Int, localize: Boolean, notifyNeighbors: Boolean) {
        val (localBlock, localData) =
            if(localize) world.localizedBlockAndDataAt(xyz, block, data) else (block, data)
        val i = index(xyz)
        blocks(i) = localBlock
        datas.foreach(_(i) = unsignedByte(localData))
    }

    protected def setEntityAtValid(xyz: XYZ, entity: TileEntity) { }

    protected def setBiomeAtValid(xz: XZ, biome: Biome) {
        biomes.foreach(_(index(xz)) = biome)
    }
}
