package farseek.world.gen.structure

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import farseek.util.BoundingBox._
import farseek.util.ImplicitConversions._
import farseek.util._
import farseek.world._
import farseek.world.gen.CoordinateSystem._
import farseek.world.gen._
import net.minecraft.world._
import net.minecraft.world.gen.structure.MapGenStructure
import net.minecraftforge.common.MinecraftForge._
import net.minecraftforge.event.terraingen._
import net.minecraftforge.event.world.WorldEvent
import scala.collection.mutable

/** Farseek implementation of world generation [[Structure]] generators, as an alternative to vanilla [[MapGenStructure]]s.
  * @author delvr
  */
abstract class StructureGenerator[T <: Structure] extends Logging {

    protected val structures = mutable.Map[XZ, Option[T]]()

    def generate(xzChunk: XZ)(implicit r: BlockReader)

    def carve(implicit w: BlockWriter with ChunkSet)

    def build(implicit w: BlockWriter with ChunkSet)

    def clear() { structures.clear() }

    protected def createIn(zone: Bounded)(implicit w: BlockReader): Option[T]

    protected def generateFrom(xzChunk: XZ, zone: Bounded)(implicit r: BlockReader) {
        structures(xzChunk) = createIn(zone).filter(_.generateIn(r, chunkRandomSeed(xzChunk)))
    }

    protected def carveFrom(xzChunk: XZ)(implicit w: BlockWriter with ChunkSet) {
        doWithIntersectingStructureAt(xzChunk, _.carveIn(w))
    }

    protected def buildFrom(xzChunk: XZ)(implicit w: BlockWriter with ChunkSet) {
        doWithIntersectingStructureAt(xzChunk, _.buildIn(w))
    }

    private def doWithIntersectingStructureAt(xzChunk: XZ, f: Structure => Unit)(implicit w: Bounded) {
        structures.get(xzChunk).foreach(_.foreach( structure =>
            if(structure.intersects(w)) f(structure)
        ))
    }
}

object StructureGenerator {

    EVENT_BUS.register(this)

    private val dimensionGenerators =
        mutable.Map[Int, mutable.Map[WorldType, mutable.Buffer[StructureGenerator[_]]]]().withDefaultValue(
            mutable.Map().withDefaultValue(mutable.Buffer()))

    def generatorsFor(world: World): Seq[StructureGenerator[_]] =
        dimensionGenerators(world.dimensionId)(world.terrainType)

    def addGenerator(dimensionId: Int, generator: StructureGenerator[_], worldTypes: WorldType*) {
        val dimGenerators = dimensionGenerators.getOrElseUpdate(dimensionId, mutable.Map())
        worldTypes.foreach(dimGenerators.getOrElseUpdate(_, mutable.Buffer()) += generator)
    }

    @SubscribeEvent def onPrePopulateChunk(event: PopulateChunkEvent.Pre) {
        val w = event.world
        val (xChunk, zChunk) = (event.chunkX, event.chunkZ)
        val area = new BlockWriterWrapper(WorldBlockWriter(w),
            sizedBox(xChunk*ChunkSize + ChunkSize/2, w.yMin, zChunk*ChunkSize + ChunkSize/2, ChunkSize, w.height),
            chunkRandomSeed((xChunk, zChunk), w.getSeed),
            Set((xChunk, zChunk), (xChunk + 1, zChunk), (xChunk, zChunk + 1), (xChunk + 1, zChunk + 1)))
        generatorsFor(w).foreach(_.build(area))
    }

    @SubscribeEvent def onWorldUnload(event: WorldEvent.Unload) {
        if(!event.world.isRemote)
            generatorsFor(event.world).foreach(_.clear())
    }
}

abstract class ZonedStructureGenerator[T <: Structure](xZoneChunkSize: Int, zZoneChunkSize: Int)
        extends StructureGenerator[T] {

    require(xZoneChunkSize > 0 && zZoneChunkSize > 0)

    private def xzZoneChunk(xzChunk: XZ) =
        (flooredDivision(xzChunk.x, xZoneChunkSize) * xZoneChunkSize,
         flooredDivision(xzChunk.z, zZoneChunkSize) * zZoneChunkSize)

    def generate(xzChunk: XZ)(implicit r: BlockReader) {
        val key = xzZoneChunk(xzChunk)
        if(!structures.contains(key)) {
            val zone = sizedBox(key.x*ChunkSize, r.yMin, key.z*ChunkSize,
                                xZoneChunkSize*ChunkSize, r.height, zZoneChunkSize*ChunkSize)
            generateFrom(key, zone)
        }
    }

    def carve(implicit w: BlockWriter with ChunkSet) {
        w.xzChunks.map(xzZoneChunk).foreach(carveFrom)
    }

    def build(implicit w: BlockWriter with ChunkSet) {
        w.xzChunks.map(xzZoneChunk).foreach(buildFrom)
    }
}

abstract class RangedStructureGenerator[T <: Structure](chunksRange: Int) extends StructureGenerator[T] {

    require(chunksRange > 0)

    def generate(xzChunk: XZ)(implicit r: BlockReader) {
        for(xStructureChunk <- xzChunk.x - chunksRange to xzChunk.x + chunksRange;
            zStructureChunk <- xzChunk.z - chunksRange to xzChunk.z + chunksRange) {
            val key = (xStructureChunk, zStructureChunk)
            if(!structures.contains(key)) {
                val zone = BoundingBox((xStructureChunk - chunksRange)*ChunkSize, r.yMin,
                                       (zStructureChunk - chunksRange)*ChunkSize,
                                       (xStructureChunk + chunksRange)*ChunkSize + iChunkMax, r.yMax,
                                       (zStructureChunk + chunksRange)*ChunkSize + iChunkMax)
                generateFrom(key, zone)
            }
        }
    }

    def carve(implicit w: BlockWriter with ChunkSet) {
        structures.keys.foreach(carveFrom)
    }

    def build(implicit w: BlockWriter with ChunkSet) {
        structures.keys.foreach(buildFrom)
    }
}
