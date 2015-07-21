package farseek.world

import com.bioxx.tfc.Blocks.Terrain.BlockCollapsible
import farseek.util.Reflection._
import farseek.util.{XYZ, _}
import farseek.world.gen.ChunkGeneratorExtensions._
import java.lang.reflect.Field
import net.minecraft.block.BlockFalling
import net.minecraft.world._
import net.minecraft.world.chunk._
import scala.collection.mutable
import scala.util.Random

/** World generation utilities.
  * @author delvr
  */
package object gen {

    type ChunkGenerator = IChunkProvider

    /** Maps [[ChunkGenerator]]s with the first field of [[World]] type or subtype declared in their class. */
    val chunkGeneratorWorldClassFields =
        mutable.Map[Class[_ <: ChunkGenerator], Field]().withDefault(classFields[World](_).head)

    def chunkRandomSeed(xzChunk: XZ)(implicit r: BlockReader): Long = chunkRandomSeed(xzChunk, r.randomSeed)

    def chunkRandomSeed(xzChunk: XZ, worldSeed: Long): Long = {
        val random = new Random(worldSeed)
        xzChunk.x.toLong * random.nextLong ^ xzChunk.z.toLong * random.nextLong ^ worldSeed
    }

    /** Returns true if world generation is currently in the "populating" step (chunk decoration). */
    def populating = BlockFalling.fallInstantly || (tfcLoaded && BlockCollapsible.fallInstantly) || populatingExtras

    /** Returns true if a falling block at `xyz` should teleport to its landing position instead of turning into a
      * falling entity. This the case during chunk population and where any chunks in a 65-block cube centered on
      * `xyz` are missing. */
    def blocksFallInstantlyAt(xyz: XYZ)(implicit w: World) = {
        val (x, y, z) = xyz
        BlockFalling.fallInstantly || (tfcLoaded && BlockCollapsible.fallInstantly) ||
        !w.checkChunksExist(x - 32, y - 32, z - 32, x + 32, y + 32, z + 32)
    }

    /** Value class for [[ChunkGenerator]]s with utility methods. */
    implicit class ChunkGeneratorValue(val generator: ChunkGenerator) extends AnyVal {
        def world: WorldServer = chunkGeneratorWorldClassFields(generator.getClass)(generator)
    }

    /** Value class for [[WorldServer]]s with utility methods. */
    implicit class WorldServerValue(val world: WorldServer) extends AnyVal {
        def chunkGenerator: ChunkGenerator = world.theChunkProviderServer.currentChunkProvider
    }
}
