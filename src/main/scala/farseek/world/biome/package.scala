package farseek.world

import farseek.block._
import net.minecraft.world.biome._

/** Utility methods and value classes related to [[BiomeGenBase]] objects.
  * @author delvr
  */
package object biome {

    type Biome = BiomeGenBase

    /** Value class for [[Biome]] objects with utility methods. */
    implicit class BiomeValue(val biome: Biome) extends AnyVal {

        /** Returns the base biome for a [[BiomeGenMutated]], or the biome itself otherwise.*/
        def base: Biome = biome match {
            case mutant: BiomeGenMutated => mutant.baseBiome
            case _ => biome
        }

        def topBlockAndData: BlockAndData = (biome.topBlock, biome.field_150604_aj)
        def fillerBlockAndData: BlockAndData = (biome.fillerBlock, biome.field_76754_C)
    }
}
