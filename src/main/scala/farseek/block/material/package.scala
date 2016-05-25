package farseek.block

import net.minecraft.block.material._
import net.minecraft.block._
import net.minecraft.init.Blocks._
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry._

/** Utility methods and value classes related to block [[Material]]s.
  * @author delvr
  */
package object material {

    /** Value class for [[MaterialLiquid]]s with utility methods. */
    implicit class LiquidMaterialValue(val material: MaterialLiquid) extends AnyVal {

        /** Returns the still liquid block for this material if it is water or lava, or throws an exception otherwise. */
        def stillBlock: BlockLiquid = (material match {
            case Material.water => water
            case Material.lava  => lava
        }).asInstanceOf[BlockLiquid]

        /** Returns the flowing liquid block for this material if it is water or lava, or throws an exception otherwise. */
        def flowingBlock: BlockDynamicLiquid = material match {
            case Material.water => flowing_water
            case Material.lava  => flowing_lava
        }

        /** Returns the [[net.minecraftforge.fluids.Fluid]] for this material if it is water or lava, or throws an exception otherwise. */
        def fluid: Fluid = material match {
            case Material.water => WATER
            case Material.lava  => LAVA
        }
    }
}
