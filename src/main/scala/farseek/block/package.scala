package farseek

import com.bioxx.tfc.Core.TFCTabs._
import com.bioxx.tfc.Core.TFC_Core
import com.bioxx.tfc.Core.TFC_Core._
import farseek.util.ImplicitConversions._
import farseek.util._
import farseek.block.material._
import net.minecraft.block.Block._
import net.minecraft.block._
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs._
import net.minecraft.init.Blocks._
import net.minecraft.item.Item._
import net.minecraft.item._
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.oredict.OreDictionary._
import scala.collection.mutable

/** Utility functions and value classes related to [[Block]]s and their metadata. Note: to ensure that all custom blocks
  * have been loaded by mods, do not access this object until mod initialization is complete.
  * @author delvr
  */
package object block {

    type BlockAndData = (Block, Int)

    val DataValues = 0 to 15

    val normalDirt = (dirt, 0)
    val coarseDirt = (dirt, 1)
    val podzol     = (dirt, 2)
    val yellowSand = (sand, 0)
    val redSand    = (sand, 1)

    val White = 0
    val Orange = 1
    val Magenta = 2
    val LightBlue = 3
    val Yellow = 4
    val Lime = 5
    val Pink = 6
    val Gray = 7
    val LighGray = 8
    val Cyan = 9
    val Purple = 10
    val Blue = 11
    val Brown = 12
    val Green = 13
    val Red = 14
    val Black = 15

    /** All blocks defined in the block registry. */
    lazy val allBlocks: Set[Block] = blockRegistry.registryObjects.values.toSet[Block]

    /** Blocks from [[allBlocks]] for which the best harvesting tool is the shovel. */
    lazy val granularBlocks: Set[Block] = {
        Class.forName(classOf[ForgeHooks].getName) // Ensure static initializer execution
        allBlocks.filter(block => block.isSolid && block.getHarvestTool(0) == "shovel")
    }

    /** Blocks from [[granularBlocks]] that have ground or grass as material.
      * (For TFC, filters with [[TFC_Core.isSoil]] instead). */
    lazy val soilBlocks: Set[Block] = granularBlocks.filter(block =>
        if(tfcLoaded) isSoil(block) else block.material == Material.ground || block.material == Material.grass)

    /** Blocks from [[granularBlocks]] that have clay or sand (this includes gravel) as material.
      * (For TFC, also includes those for which [[TFC_Core.isGravel]] is `true`). */
    lazy val sedimentBlocks: Set[Block] = granularBlocks.filter(block =>
        block.material == Material.sand || block.material == Material.clay || (tfcLoaded && isGravel(block)))

    /** Blocks from the Forge ore dictionary registered as "stone" or "sandstone" or any string starting with "ore",
      * as well as the specific blocks hardened clay, stained hardened clay, netherrack, End stone and bedrock. */
    lazy val naturalStoneBlocks: Set[Block] =
        dictionaryBlocks(_.contains("stone")) ++ dictionaryBlocks(_.startsWith("ore")) +
        hardened_clay + stained_hardened_clay + netherrack + end_stone + bedrock

    /** Returns the Forge ore dictionary blocks registered with the name `key`. */
    def dictionaryBlocks(key: String): Set[Block] = dictionaryBlocksAndData(key).map(_.block)

    /** Returns the Forge ore dictionary blocks matching filter `keyFilter`. */
    def dictionaryBlocks(keyFilter: String => Boolean): Set[Block] = dictionaryBlocksAndData(keyFilter).map(_.block)

    /** Returns the Forge ore dictionary blocks registered with the name `key`, with metadata. */
    def dictionaryBlocksAndData(key: String): Set[BlockAndData] = getOres(key).flatMap(itemStackBlockAndData).toSet

    /** Returns the Forge ore dictionary blocks matching filter `keyFilter`, with metadata. */
    def dictionaryBlocksAndData(keyFilter: String => Boolean): Set[BlockAndData] =
        getOreNames.filter(name => name != null && keyFilter(name)).flatMap(dictionaryBlocksAndData).toSet

    def materialBlocks(material: Material) = allBlocks.filter(_.material == material)

    /** Value class for [[net.minecraft.block.Block]]s with utility methods. */
    implicit class BlockValue(val block: Block) extends AnyVal {

        def option: Option[Block] = if(block != air) Some(block) else None

        def material: Material = block.getMaterial
        def isSolid = material.isSolid
        def isLiquid = material.isLiquid
        def isSnow = material.isSnow
        def isIce = material.isIce
        def isLiquidOrIce = material.isLiquidOrIce
        def isSolidOrLiquid = material.isSolidOrLiquid
        def isHardSolid = material.isHardSolid

        def isGrass  = block == grass  || (tfcLoaded && TFC_Core.isGrass (block))
        def isDirt   = block == dirt   || (tfcLoaded && TFC_Core.isDirt  (block))
        def isClay   = block == clay   || (tfcLoaded && TFC_Core.isClay  (block))
        def isSand   = block == sand   || (tfcLoaded && TFC_Core.isSand  (block))
        def isGravel = block == gravel || (tfcLoaded && TFC_Core.isGravel(block))
        def isGranular     =     granularBlocks.contains(block)
        def isSoil         =         soilBlocks.contains(block)
        def isSediment     =     sedimentBlocks.contains(block)
        def isNaturalStone = naturalStoneBlocks.contains(block)
        def isGround = isSoil || isSediment || isNaturalStone
        def isDiscreteObject = !(block.displayOnCreativeTab == tabBlock ||
                                 (tfcLoaded && block.displayOnCreativeTab == TFCBuilding))

        def displayNames: Seq[String] = {
            val subBlocks = new java.util.ArrayList[ItemStack](DataValues.size)
            block.getSubBlocks(getItemFromBlock(block), block.displayOnCreativeTab, subBlocks)
            val sb: mutable.Buffer[ItemStack] = subBlocks
            sb.map(_.getDisplayName)
        }
    }

    /** Value class for a block-and-metadata tuple to allow member access by name. */
    implicit class BlockAndDataValue(val bd: BlockAndData) extends AnyVal {
        def block = bd._1
        def data  = bd._2
    }
}
