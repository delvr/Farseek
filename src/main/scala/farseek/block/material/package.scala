package farseek.block

import com.bioxx.tfc.api._
import farseek.util._
import net.minecraft.block.material.{Material, MaterialLiquid}
import net.minecraft.block.{BlockDynamicLiquid, BlockLiquid}
import net.minecraft.init.Blocks._
import net.minecraftforge.fluids.{Fluid, FluidRegistry}

/** Utility methods and value classes related to block [[Material]]s.
  * @author delvr
  */
package object material {

    /** Value class for [[MaterialLiquid]]s with utility methods. Assumes fresh water for TFC purposes. */
    implicit class LiquidMaterialValue(val material: MaterialLiquid) extends AnyVal {

        /** Returns the still liquid block for this material if it is water or lava, or thows an exception otherwise.
          * (Returns the relevant TFC block if TFC is loaded.) */
        def stillBlock: BlockLiquid = (material match {
            case Material.water => if(tfcLoaded) TFCBlocks.freshWaterStationary else water
            case Material.lava  => if(tfcLoaded) TFCBlocks.lavaStationary else lava
        }).asInstanceOf[BlockLiquid]

        /** Returns the flowing liquid block for this material if it is water or lava, or thows an exception otherwise.
          * (Returns the relevant TFC block if TFC is loaded.) */
        def flowingBlock: BlockDynamicLiquid = (material match {
            case Material.water => if(tfcLoaded) TFCBlocks.freshWater else flowing_water
            case Material.lava  => if(tfcLoaded) TFCBlocks.lava else flowing_lava
        }).asInstanceOf[BlockDynamicLiquid]

        /** Returns the [[net.minecraftforge.fluids.Fluid]] for this material if it is water or lava, or thows an exception otherwise.
          * (Returns the relevant TFC fluid if TFC is loaded.) */
        def fluid: Fluid = material match {
            case Material.water => if(tfcLoaded) TFCFluids.FRESHWATER else FluidRegistry.WATER
            case Material.lava  => if(tfcLoaded) TFCFluids.LAVA else FluidRegistry.LAVA
        }
    }
}
