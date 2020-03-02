package farseek.world.gen.structure

import farseek.util.ImplicitConversions._
import farseek.util.Reflection._
import farseek.util._
import farseek.world._
import farseek.world.gen._
import java.util.Random
import net.minecraft.world._
import net.minecraft.world.chunk._
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.common.MinecraftForge._
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks
import net.minecraftforge.event.terraingen._
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import scala.collection.mutable

/** Farseek implementation of world generation [[Structure]] generators, as an alternative to vanilla [[net.minecraft.world.gen.structure.MapGenStructure]]s.
  *
  * @migration(message = "Structure API is not fully stable and will change for Streams version 1.0", version = "1.1.0")
  * @author delvr
  */
abstract class StructureGenerator[T <: Structure[_]](chunksRange: Int, dimensionId: Int = SurfaceDimensionId) extends Logging {

    private var generating = false // Avoid recursion caused by mods scanning chunks during ChunkGenerator initialization

    protected val invalidWorldTypes = Set[WorldType]()
    protected val structures = mutable.Map[XZ, Option[T]]()

    EVENT_BUS.register(this)

    @SubscribeEvent def onChunkGeneration(event: ReplaceBiomeBlocks) {
        val world =
            if(event.getWorld != null) event.getWorld
            else chunkGeneratorWorldClassFields.getOrElseUpdate(event.getGenerator.getClass,
              classFields[World](event.getGenerator.getClass).head).value[World](event.getGenerator)
        onChunkGeneration(world.asInstanceOf[WorldServer], event.getGenerator, event.getX, event.getZ, event.getPrimer)
    }

    def onChunkGeneration(world: WorldServer, generator: IChunkGenerator, xChunk: Int, zChunk: Int, primer: ChunkPrimer) {
        if(!generating && world.getDimension == dimensionId && !invalidWorldTypes.contains(world.terrainType)) {
            // Don't recurse events when generating for structures
//            println("Event generator: " + generator)
//            println("World generator: " + world.getChunkProvider.chunkGenerator)
//            println("Streams generator: " + StructureGenerationChunkProvider(world).generator)
            if(generator != StructureGenerationChunkProvider(world).generator) {
                generating = true
                generate(world, xChunk, zChunk, primer)
                generating = false
            }
        }
    }

    protected def generate(world: WorldServer, xChunk: Int, zChunk: Int, primer: ChunkPrimer) {
        for(xStructureChunk <- xChunk - chunksRange to xChunk + chunksRange;
            zStructureChunk <- zChunk - chunksRange to zChunk + chunksRange) {
            if(!structures.contains(xStructureChunk, zStructureChunk)) {
              implicit val chunkProvider = new StructureGenerationBlockAccess(StructureGenerationChunkProvider(world))
              implicit val random = chunkRandom(xStructureChunk, zStructureChunk)(world)
              val bounds = worldHeightBox((xStructureChunk - chunksRange)*ChunkSize,             (zStructureChunk - chunksRange)*ChunkSize,
                                          (xStructureChunk + chunksRange)*ChunkSize + iChunkMax, (zStructureChunk + chunksRange)*ChunkSize + iChunkMax)
                val structureOption = createStructure(bounds).flatMap { structure =>
                    structure.generate()
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
        if(event.getWorld.getDimension == this.dimensionId)
            build(event.getChunkX, event.getChunkZ)(event.getWorld.asInstanceOf[WorldServer], event.getRand)
    }

    protected def build(xChunk: Int, zChunk: Int)(implicit world: WorldServer, random: Random) {
        val area = new PopulatingArea(xChunk, zChunk, world)
        structures.foreach {
            case (_, Some(structure)) if structure.intersectsWith(area) => structure.build(area, random)
            case _ =>
        }
    }

    @SubscribeEvent def onWorldUnload(event: WorldEvent.Unload) {
        if(!event.getWorld.isRemote && event.getWorld.getDimension == this.dimensionId)
            structures.clear()
    }
}
