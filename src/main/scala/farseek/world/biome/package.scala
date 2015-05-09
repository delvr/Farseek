package farseek.world

import com.bioxx.tfc.Core.TFC_Climate._
import com.bioxx.tfc.Core.TFC_Core._
import farseek.block._
import farseek.util.ImplicitConversions._
import farseek.util._
import net.minecraft.block._
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

        def topBlockAndData: BlockAndData = (biome.topBlock, biome.field_150604_aj)
        def fillerBlockAndData: BlockAndData = (biome.fillerBlock, biome.field_76754_C)
    }

    /** Returns the vanilla grass block, or the appropriate TFC grass block if TFC is loaded. */
    def grassBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        if(tfcLoaded) tfcGroundBlockAndData(x, y, z, layerData => getTypeForGrassWithRain(layerData, tfcRainfall(x, y, z)))
        else grass

    /** Returns the vanilla dirt block, or the appropriate TFC dirt block if TFC is loaded. */
    def dirtBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        if(tfcLoaded) tfcGroundBlockAndData(x, y, z, getTypeForDirt)
        else dirt

    /** Returns the vanilla biome-specific sand block, or the appropriate TFC sand block if TFC is loaded. */
    def sandBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        if(tfcLoaded) tfcGroundBlockAndData(x, y, z, getTypeForSand)
        else {
            val biome = baseBiomeAt(x, y, z)
            if(biome.topBlock.isSand) biome.topBlockAndData
            else sand
        }

    /** Returns the vanilla gravel block, or the appropriate TFC gravel block if TFC is loaded. */
    def gravelBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData =
        if(tfcLoaded) tfcGroundBlockAndData(x, y, z, getTypeForGravel)
        else gravel

    /** Returns the vanilla biome-specific rock block (always stone if the block above is stone), or the appropriate TFC rock block if TFC is loaded. */
    def rockBlockFor(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): BlockAndData = {
        if(blockAbove(x, y, z) == stone)
            stone
        else baseBiomeAt(x, y, z) match {
            case nether: BiomeGenHell => netherrack
            case end: BiomeGenEnd => end_stone
            case tfcBiome if tfcLoaded =>
                val layer = tfcLayer(x, y, z)
                (layer.block, layer.data2)
            case biome =>
                val top = biome.topBlockAndData
                if(top == yellowSand) sandstone
                else if(top == redSand) (stained_hardened_clay, Orange)
                else stone
        }
    }

    private def tfcGroundBlockAndData(x: Int, y: Int, z: Int, f: Int => Block)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates): (Block, Int) = {
        val layer = tfcLayer(x, y, z)
        (f(layer.data1), getSoilMeta(layer.data1))
    }

    private def tfcLayer(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = {
        val w = bac.worldProvider.worldObj
        val (wx, wy, wz) = cs.xyzWorld(x, y, z)
        getRockLayer(w, wx, wy, wz, getRockLayerFromHeight(w, wx, wy, wz))
    }

    private def tfcRainfall(x: Int, y: Int, z: Int)(implicit bac: IBlockAccess, cs: CoordinateSystem = AbsoluteCoordinates) = {
        val w = bac.worldProvider.worldObj
        val (wx, wy, wz) = cs.xyzWorld(x, y, z)
        getRainfall(w, wx, wy, wz)
    }
}
