package farseek.world

import farseek.block._
import farseek.util.ImplicitConversions._
import net.minecraft.init.Blocks._
import net.minecraft.world._
import net.minecraft.world.biome._

/** Utility methods and value classes related to [[Biome]] objects.
  * @author delvr
  */
package object biome {

    /** Value class for [[Biome]] objects with utility methods. */
    implicit class BiomeValue(val biome: Biome) extends AnyVal {

        /** Returns the non-null base biome for a mutated biome, or the biome itself otherwise.*/
        def base: Biome = if(biome.isMutation) Option(Biome.getBiomeForId(Biome.MUTATION_TO_BASE_ID_MAP.get(biome))).getOrElse(biome) else biome
    }

    /** Returns the vanilla grass block. */
    def grassBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        GRASS

    /** Returns the vanilla dirt block. */
    def dirtBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        DIRT

    /** Returns the vanilla biome-specific sand block. */
    def sandBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData = {
        val biome = baseBiomeAt(x, y, z)
        if(biome.topBlock.getBlock.isSand) biome.topBlock
        else SAND
    }

  /** Returns the vanilla gravel block. */
    def gravelBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        GRAVEL

    def rockBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData = {
    /** Returns the vanilla biome-specific rock block (always stone if the block above is stone). */
        if(blockAbove(x, y, z) == STONE)
            STONE
        else baseBiomeAt(x, y, z) match {
            case nether: BiomeHell => NETHERRACK
            case end: BiomeEnd => END_STONE
            case biome =>
                val top = blockStateData(biome.topBlock)
                if(top == yellowSand) SANDSTONE
                else if(top == redSand) (STAINED_HARDENED_CLAY, Orange)
                else STONE
        }
    }
}
