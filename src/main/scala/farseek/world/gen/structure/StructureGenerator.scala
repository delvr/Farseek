package farseek.world.gen.structure

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import farseek.util.ImplicitConversions._
import farseek.util.Reflection._
import farseek.util._
import farseek.world._
import farseek.world.gen._
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.world._
import net.minecraftforge.common.MinecraftForge._
import net.minecraftforge.event.terraingen.ChunkProviderEvent.ReplaceBiomeBlocks
import net.minecraftforge.event.terraingen._
import net.minecraftforge.event.world.WorldEvent
import scala.collection.mutable

/** Farseek implementation of world generation [[Structure]] generators, as an alternative to vanilla [[net.minecraft.world.gen.structure.MapGenStructure]]s.
  *
  * @migration(message = "Structure API is not fully stable and will change for Streams version 0.2", version = "1.1.0")
  * @author delvr
  */
abstract class StructureGenerator[T <: Structure[_]](chunksRange: Int, dimensionId: Int = SurfaceDimensionId) extends Logging {

    protected val invalidWorldTypes = Set[WorldType]()
    protected val structures = mutable.Map[XZ, Option[T]]()

    EVENT_BUS.register(this)

    @SubscribeEvent def onChunkGeneration(event: ReplaceBiomeBlocks) {
        // Fix missing/invalid pieces from deprecated ReplaceBiomeBlocks constructors
        val world =
            if(event.world != null) event.world
            else chunkGeneratorWorldClassFields(event.chunkProvider.getClass).value[World](event.chunkProvider)
        val datas =
            if(event.metaArray != null && event.metaArray.size == event.blockArray.size) event.metaArray
            else null
        onChunkGeneration(world.provider, event.chunkProvider, event.chunkX, event.chunkZ, event.blockArray, datas)
    }

    def onChunkGeneration(worldProvider: WorldProvider, generator: ChunkGenerator, xChunk: Int, zChunk: Int, blocks: Array[Block], datas: Array[Byte]) {
        if(worldProvider.dimensionId == this.dimensionId && !invalidWorldTypes.contains(worldProvider.terrainType)) {
            implicit val worldAccess = StructureGenerationChunkProvider(worldProvider)
            if(generator != worldAccess.generator) // Don't recurse events when generating for structures
                generate(xChunk, zChunk, blocks, datas)
        }
    }

    protected def generate(xChunk: Int, zChunk: Int, blocks: Array[Block], datas: Array[Byte])(implicit worldAccess: IBlockAccess) {
        for(xStructureChunk <- xChunk - chunksRange to xChunk + chunksRange;
            zStructureChunk <- zChunk - chunksRange to zChunk + chunksRange) {
            if(!structures.contains(xStructureChunk, zStructureChunk)) {
                implicit val random = chunkRandom(xStructureChunk, zStructureChunk)(worldAccess.worldProvider)
                val bounds = worldHeightBox((xStructureChunk - chunksRange)*ChunkSize,            (zStructureChunk - chunksRange)*ChunkSize,
                                            (xStructureChunk + chunksRange)*ChunkSize + iChunkMax, (zStructureChunk + chunksRange)*ChunkSize + iChunkMax)
                val structureOption = createStructure(bounds).flatMap { structure =>
                    structure.generate(worldAccess, random)
                    if(structure.isValid) {
                        structure.commit()
                        debug(structure.debug)
                        Some(structure)
                    } else {
                        structure.clear()
                        None
                    }
                }
                structures((xStructureChunk, zStructureChunk)) = structureOption
            }
        }
    }

    protected def createStructure(bounds: BoundingBox)(implicit worldAccess: IBlockAccess, random: Random): Option[T]

    @SubscribeEvent def onPrePopulateChunk(event: PopulateChunkEvent.Pre) {
        if(event.world.dimensionId == this.dimensionId)
            build(event.chunkX, event.chunkZ)(event.world.asInstanceOf[WorldServer], event.rand)
    }

    protected def build(xChunk: Int, zChunk: Int)(implicit world: WorldServer, random: Random) {
        val area = new PopulatingArea(xChunk, zChunk, world)
        structures.foreach {
            case (_, Some(structure)) if structure.intersectsWith(area) => structure.build(area, random)
            case _ =>
        }
    }

    @SubscribeEvent def onWorldUnload(event: WorldEvent.Unload) {
        if(!event.world.isRemote && event.world.dimensionId == this.dimensionId)
            structures.clear()
    }
}
