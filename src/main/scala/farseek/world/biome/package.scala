package farseek.world

import farseek.block._
import farseek.util.ImplicitConversions._
import net.minecraft.init.Blocks._
import net.minecraft.world._
import net.minecraft.world.biome._

/** Utility methods and value classes related to [[BiomeGenBase]] objects.
  * @author delvr
  */
package object biome {

    /** Value class for [[BiomeGenBase]] objects with utility methods. */
    implicit class BiomeValue(val biome: BiomeGenBase) extends AnyVal {

        /** Returns the base biome for a [[BiomeGenMutated]], or the biome itself otherwise.*/
        def base: BiomeGenBase = biome match {
            case mutant: BiomeGenMutated => mutant.baseBiome
            case _ => biome
        }
    }

    /** Returns the vanilla grass block. */
    def grassBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        grass

    /** Returns the vanilla dirt block. */
    def dirtBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
         dirt

    /** Returns the vanilla biome-specific sand block. */
    def sandBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData = {
        val biome = baseBiomeAt(x, y, z)
        if(biome.topBlock.getBlock.isSand) biome.topBlock
        else sand
    }

    def gravelBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
    /** Returns the vanilla gravel block. */
        gravel

    def rockBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData = {
    /** Returns the vanilla biome-specific rock block (always stone if the block above is stone). */
        if(blockAbove(x, y, z) == stone)
            stone
        else baseBiomeAt(x, y, z) match {
            case nether: BiomeGenHell => netherrack
            case end: BiomeGenEnd => end_stone
            case biome =>
                val top = blockStateData(biome.topBlock)
                if(top == yellowSand) sandstone
                else if(top == redSand) (stained_hardened_clay, Orange)
                else stone
        }
    }
}
