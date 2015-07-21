package farseek.block

import com.bioxx.tfc.api.TFCBlocks._
import com.bioxx.tfc.api.TFCFluids
import farseek.util._
import net.minecraft.block.material.Material._
import net.minecraft.block.material.{Material, MaterialLiquid}
import net.minecraft.block.{BlockDynamicLiquid, BlockLiquid}
import net.minecraft.init.Blocks
import net.minecraftforge.fluids.{Fluid, FluidRegistry}

/** Utility methods and value classes related to block [[Material]]s.
  * @author delvr
  */
package object material {

    /** Value class for [[MaterialLiquid]]s with utility methods. */
    implicit class MaterialValue(val material: Material) extends AnyVal {

        def isSnow = material == snow || material == craftedSnow
        def isIce = material == ice || material == packedIce
        def isLiquidOrIce = material.isLiquid || isIce
        def isSolidOrLiquid = material.isSolid || material.isLiquid
        def isHardSolid = material.isSolid && !isSnow && !isIce && material != leaves

        def string = material match {
            case `air` => "air"
            case `grass` => "grass"
            case `ground` => "ground"
            case `wood` => "wood"
            case `rock` => "rock"
            case `iron` => "iron"
            case `anvil` => "anvil"
            case `water` => "water"
            case `lava` => "lava"
            case `leaves` => "leaves"
            case `plants` => "plants"
            case `vine` => "vine"
            case `sponge` => "sponge"
            case `cloth` => "cloth"
            case `fire` => "fire"
            case `sand` => "sand"
            case `circuits` => "circuits"
            case `carpet` => "carpet"
            case `glass` => "glass"
            case `redstoneLight` => "redstone"
            case `tnt` => "TNT"
            case `coral` => "coral"
            case `ice` => "ice"
            case `packedIce` => "packed ice"
            case `snow` => "snow"
            case `craftedSnow` => "crafted snow"
            case `cactus` => "cactus"
            case `clay` => "clay"
            case `gourd` => "gourd"
            case `dragonEgg` => "dragon egg"
            case `portal` => "portal"
            case `cake` => "cake"
            case `web` => "web"
            case _ => material.getClass.getSimpleName
        }
    }

    /** Value class for [[MaterialLiquid]]s with utility methods. Assumes fresh, non-hotspring water for TFC purposes. */
    implicit class LiquidMaterialValue(val material: MaterialLiquid) extends AnyVal {

        /** Returns the still liquid block for this material if it is water or lava, or throws an exception otherwise.
          * (Returns the relevant TFC block (fresh if water) if TFC is loaded.) */
        def stillBlock: BlockLiquid = (material match {
            case Material.water => if(tfcLoaded) FreshWaterStationary else Blocks.water
            case Material.lava  => if(tfcLoaded) LavaStationary else Blocks.lava
        }).asInstanceOf[BlockLiquid]

        /** Returns the flowing liquid block for this material if it is water or lava, or throws an exception otherwise.
          * (Returns the relevant TFC block (fresh if water) if TFC is loaded.) */
        def flowingBlock: BlockDynamicLiquid = (material match {
            case Material.water => if(tfcLoaded) FreshWater else Blocks.flowing_water
            case Material.lava  => if(tfcLoaded) Lava else Blocks.flowing_lava
        }).asInstanceOf[BlockDynamicLiquid]

        /** Returns the [[Fluid]] for this material if it is water or lava, or throws an exception otherwise.
          * (Returns the relevant TFC fluid (fresh if water) if TFC is loaded.) */
        def fluid: Fluid = material match {
            case Material.water => if(tfcLoaded) TFCFluids.FRESHWATER else FluidRegistry.WATER
            case Material.lava  => if(tfcLoaded) TFCFluids.LAVA else FluidRegistry.LAVA
        }
    }
}
