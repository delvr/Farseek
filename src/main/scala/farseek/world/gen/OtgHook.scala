package farseek.world.gen

import com.pg85.otg.forge.ForgeWorld
import com.pg85.otg.forge.generator._
import com.pg85.otg.generator._
import farseek.util.Reflection._
import net.minecraft.world.WorldServer
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks

object OtgHook {

  private val otgGenerateTerrain = classOf[ChunkProviderOTG ].getDeclaredMethod("generateTerrain", classOf[ChunkBuffer]).accessible
  private val otgWorld           = classOf[ChunkProviderOTG ].getDeclaredField("localWorld").accessible
  private val otgGenerator       = classOf[OTGChunkGenerator].getDeclaredField("generator").accessible

  def generateTerrain(generator: ChunkProviderOTG, chunkBuffer: ChunkBuffer): Unit = {
    otgGenerateTerrain(generator, chunkBuffer)
    val world = otgWorld(generator).asInstanceOf[ForgeWorld].world.asInstanceOf[WorldServer]
    val worldChunkGenerator = world.getChunkProvider.chunkGenerator.asInstanceOf[OTGChunkGenerator]
    if(generator eq otgGenerator(worldChunkGenerator)) {
      val chunkCoords = chunkBuffer.getChunkCoordinate
      EVENT_BUS.post(new ReplaceBiomeBlocks(worldChunkGenerator, chunkCoords.getChunkX, chunkCoords.getChunkZ,
          chunkBuffer.getClass.getDeclaredField("chunkPrimer").accessible(chunkBuffer), world))
    }
  }
}
