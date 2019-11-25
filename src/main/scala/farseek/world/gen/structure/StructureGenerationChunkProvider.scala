package farseek.world.gen.structure

import net.minecraft.world.{World, WorldServer}
import scala.collection.mutable

/** A chunk provider for [[Structure]]s that creates chunks with only raw terrain and _without_ structures.
  *
  * These chunks are used for structures being generated to query the terrain in their range without triggering recursion by generating more of themselves.
  * This enables Farseek structures to be terrain-aware in a way that vanilla structures cannot be, but also means that structures cannot
  * know of others that can appear in their range. The implementor is responsible for either preventing these situations (ex.: by partitioning
  * the world into distinct areas for generation, as is done for Streams) or handling collisions gracefully.
  *
  * @author delvr
  */
case class StructureGenerationChunkProvider(world: World, generator: (Int, Int) => Unit)

/** Companion object for [[StructureGenerationChunkProvider]]s, that maintains a mapping of [[World]]s to `StructureGenerationChunkProvider`s.
  * @author delvr
  */
object StructureGenerationChunkProvider {

    val providers: mutable.Map[World, StructureGenerationChunkProvider] = mutable.Map[World, StructureGenerationChunkProvider]()

    def apply(world: WorldServer): StructureGenerationChunkProvider = providers(world)
}
