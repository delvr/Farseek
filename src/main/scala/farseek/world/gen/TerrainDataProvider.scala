package farseek.world.gen

import com.bioxx.tfc.WorldGen.MapGen.MapGenBaseTFC
import com.bioxx.tfc.WorldGen.TFCChunkProviderGenerate
import cpw.mods.fml.common.eventhandler._
import farseek.core.ReplacedMethod
import farseek.util.ImplicitConversions._
import farseek.util.Reflection._
import farseek.util._
import farseek.world._
import farseek.world.biome._
import farseek.world.gen.structure.Structure
import farseek.world.gen.structure.StructureGenerator._
import net.minecraft.block.Block
import net.minecraft.init.Blocks._
import net.minecraft.world._
import net.minecraft.world.gen._
import net.minecraft.world.gen.structure.MapGenStructure
import net.minecraftforge.event.terraingen.ChunkProviderEvent.ReplaceBiomeBlocks
import scala.collection.mutable

/** A chunk data provider for [[Structure]]s that contains a copy of a world's chunk generator and creates raw chunk
  * data _without_ structures.
  *
  * These "chunks" are used for structures being generated to query the terrain in their range without triggering
  * recursion by generating more of themselves. This enables Farseek structures to be terrain-aware in a way that
  * vanilla structures cannot be, but also means that structures cannot know of others that can appear in their range.
  * The implementor is responsible for either preventing these situations (ex.: by partitioning the world into
  * distinct areas for generation, as is done for Streams) or handling collisions gracefully.
  *
  * @author delvr
  */
abstract class TerrainDataProvider(world: World) extends BlockReader {

    private val wbw = WorldBlockWriter(world)

    val bounds     = wbw.bounds

    val randomSeed = wbw.randomSeed

    private val terrainDatas = mutable.Map[XZ, ChunkGenerationData]()

    protected val generator = world.createChunkGenerator

    protected def yBottom = yMin

    protected def blockAtValid(xyz: XYZ) = chunkGenerationDataAt(xyz).blockAt(xyz)

    protected def dataAtValid(xyz: XYZ) = chunkGenerationDataAt(xyz).dataAt(xyz)

    protected def entityAtValid(xyz: XYZ) = chunkGenerationDataAt(xyz).entityAt(xyz)

    protected def biomeAtValid(xz: XZ) = chunkGenerationDataAt(xz).biomeAt(xz)

    protected def topNonEmptyAtValid(xz: XZ) = chunkGenerationDataAt(xz).topNonEmptyAt(xz)

    def onTerrainGeneration(world: WorldServer, generator: ChunkGenerator, xzChunk: XZ, blocks: Array[Block],
                            datas: Option[Array[Byte]] = None, biomes: Option[Array[Biome]] = None) {
        if(generator == world.chunkGenerator) {
            // Normal chunk providing case; generate and build our structures here. They will query terrain and that
            // will hit the "else" branch.
            generateStructures(world, xzChunk, blocks, datas, biomes)
        } else if(generator == this.generator) {
            // We were called by a generating structure using a ChunkGenerator copy to look ahead at terrain
            // For performance reasons, we interrupt the chunk generation process as soon as we have the data we want.
            throw TerrainGeneratedInterrupt(generateTerrainForStructures(xzChunk, blocks, datas, biomes))
        }
        // Otherwise this is a secondary generator by another mod (ex. DeeperCaves's).
        // Don't do anything or recursion will likely ensue.
    }

    def generateStructures(world: World, xzChunk: XZ, blocks: Array[Block],
                           datas: Option[Array[Byte]], biomes: Option[Array[Biome]]) {
        val gens = generatorsFor(world)
        gens.foreach(_.generate(xzChunk)(this))
        // "real" chunk is ready with all structures in range, so we don't go there again.
        // Note that since we don't recreate structures on world load, reloaded chunks can be "generated through"
        // and won't be unloaded, but this should remain a small number.
        terrainDatas.remove(xzChunk)
        // Create chunk reference data where all arrays will be modified in-place before continuing with generation.
        // Carve() is only for above ground where ground cover matters, so TFC is fine with using only the top half here
        val chunkGenerationData = new ChunkGenerationWritableData(world, xzChunk, yBottom, blocks, datas, biomes)
        gens.foreach(_.carve(chunkGenerationData))
    }

    protected def generateTerrainForStructures(xzChunk: XZ, blocks: Array[Block],
                                     datas: Option[Array[Byte]], biomes: Option[Array[Biome]]): ChunkGenerationData

    private def chunkGenerationDataAt(xz: XZ): ChunkGenerationData = {
        val xzC = xzChunk(xz)
        terrainDatas.getOrElseUpdate(xzC, generateAt(xzC))
    }

    private def generateAt(xzChunk: XZ) = {
        try {
            generator.provideChunk(xzChunk.x, xzChunk.z)
            sys.error("Recursive terrain generation")
        } catch {
            case TerrainGeneratedInterrupt(data) => data
        }
    }

    private case class TerrainGeneratedInterrupt(data: ChunkGenerationData) extends RuntimeException
}

class StandardTerrainDataProvider private(world: World) extends TerrainDataProvider(world) {

    import farseek.world.gen.StandardTerrainDataProvider._

    private val featureGenerators = classFieldValues[MapGenBase](generator).filterNot(_.isInstanceOf[MapGenStructure])

    protected def generateTerrainForStructures(xzChunk: XZ, blocks: Array[Block],
                                     datas: Option[Array[Byte]], biomes: Option[Array[Biome]]) = {
        // Optimization: we know that the vanilla generator block and data arrays are newly-allocated each time
        // so no need to copy them.
        val (storageBlocks, storageDatas) =
            if(VanillaGeneratorClasses.contains(generator.getClass)) (blocks, datas)
            else (copyOf(blocks), datas.map(copyOf))
        featureGenerators.foreach(_.func_151539_a(generator, world, xzChunk.x, xzChunk.z, storageBlocks))
        new ChunkGenerationData(world, xzChunk, yMin, storageBlocks, storageDatas, biomes.map(copyOf(_)))
    }
}

object StandardTerrainDataProvider extends WorldData[StandardTerrainDataProvider] {

    val VanillaGeneratorClasses = Set[Class[_ <: ChunkGenerator]](
        classOf[ChunkProviderGenerate], classOf[ChunkProviderHell], classOf[ChunkProviderEnd])

    protected def newData(world: World) = new StandardTerrainDataProvider(world)

    def postEvent(event: Event, super_postEvent: ReplacedMethod[EventBus])(implicit bus: EventBus): Boolean = {
        event match {
            case e: ReplaceBiomeBlocks =>
                val generator = e.chunkProvider
                val world = generator.world
                if(generatorsFor(world).nonEmpty) {
                    val terrainProvider = StandardTerrainDataProvider(world)
                    val datas = if(e.metaArray != null && e.metaArray.size == e.blockArray.size) Some(e.metaArray)
                                else None // Workaround a deprecated API used by ex.: ATG mod
                    terrainProvider.onTerrainGeneration(
                        world, generator, (e.chunkX, e.chunkZ), e.blockArray, datas, Option(e.biomeArray))
                }
            case _ =>
        }
        super_postEvent(event)
    }
}

class TfcTerrainDataProvider private(world: World) extends TerrainDataProvider(world) {

    override protected def yBottom = ChunkHeight / 2

    private val featureGenerators = classFieldValues[MapGenBaseTFC](generator)

    protected def generateTerrainForStructures(xzChunk: XZ, blocks: Array[Block],
                                     datas: Option[Array[Byte]], biomes: Option[Array[Biome]]) = {
        val storageBlocks = new Array[Block](ChunkVolume)
        for(x <- 0 until ChunkSize; z <- 0 until ChunkSize; y <- 0 until ChunkHeight) {
            storageBlocks((x, y, z).xzyFlatArrayIndex(ChunkSize, ChunkHeight)) =
                if(y < yBottom) stone else blocks((x, y - yBottom, z).xzyFlatArrayIndex(ChunkSize, ChunkHeight / 2))
        }
        featureGenerators.foreach(_.generate(generator, world, xzChunk.x, xzChunk.z, storageBlocks))
        new ChunkGenerationData(world, xzChunk, yMin, storageBlocks, datas.map(copyOf(_)), biomes.map(copyOf(_)))
    }
}

object TfcTerrainDataProvider extends WorldData[TfcTerrainDataProvider] {

    protected def newData(world: World) = new TfcTerrainDataProvider(world)

    def generateHighTerrain(xChunk: Int, zChunk: Int, topBlocks: Array[Block],
                            generateTerrainHigh: ReplacedMethod[TFCChunkProviderGenerate])
                           (implicit generator: TFCChunkProviderGenerate) {
        generateTerrainHigh(xChunk, zChunk, topBlocks)
        val world = generator.world
        if(generatorsFor(world).nonEmpty) {
            val terrainProvider = TfcTerrainDataProvider(world)
            terrainProvider.onTerrainGeneration(world, generator, (xChunk, zChunk), topBlocks)
        }
    }
}