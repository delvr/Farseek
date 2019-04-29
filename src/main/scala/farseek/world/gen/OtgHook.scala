package farseek.world.gen

import com.pg85.otg.forge.ForgeWorld
import com.pg85.otg.generator._
import farseek.util.Reflection._
import net.minecraft.world.WorldServer
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.ReplaceBiomeBlocks

object OtgHook {

    private val otgGenerateTerrain = classOf[ChunkProviderOTG].getDeclaredMethod("generateTerrain", classOf[ChunkBuffer]).accessible
    private val otgWorld           = classOf[ChunkProviderOTG].getDeclaredField("localWorld").accessible

    def generateTerrain(generator: ChunkProviderOTG, chunkBuffer: ChunkBuffer): Unit = {
        otgGenerateTerrain(generator, chunkBuffer)
        val world = otgWorld(generator).asInstanceOf[ForgeWorld].world.asInstanceOf[WorldServer]
        val chunkCoords = chunkBuffer.getChunkCoordinate
        EVENT_BUS.post(new ReplaceBiomeBlocks(world.getChunkProvider.chunkGenerator, chunkCoords.getChunkX, chunkCoords.getChunkZ,
          chunkBuffer.getClass.getDeclaredField("chunkPrimer").accessible(chunkBuffer), world))
    }
}
