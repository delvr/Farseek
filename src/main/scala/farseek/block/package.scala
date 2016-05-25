package farseek

import farseek.util.ImplicitConversions._
import net.minecraft.block._
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs._
import net.minecraft.init.Blocks._
import net.minecraft.item._
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.oredict.OreDictionary._

/** Utility functions and value classes related to [[Block]]s and their metadata.
  * Note: to ensure that all custom blocks have been loaded by mods, do not access this object until mod initialization is complete.
  * @author delvr
  */
package object block {

    type BlockAndData = (Block, Int)

    val DataValues = 0 to 15

    val normalDirt = (DIRT, 0)
    val coarseDirt = (DIRT, 1)
    val podzol     = (DIRT, 2)
    val yellowSand = (SAND, 0)
    val redSand    = (SAND, 1)

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
    lazy val allBlocks: Set[Block] = Block.REGISTRY.registryObjects.values.toSet[Block]

    /** Blocks from [[allBlocks]] for which the best harvesting tool is the shovel. */
    lazy val granularBlocks: Set[Block] = {
        Class.forName(classOf[ForgeHooks].getName) // Ensure static initializer execution
        allBlocks.filter(block => block.isSolid && block.getHarvestTool(block.getDefaultState) == "shovel")
    }

    /** Blocks from [[granularBlocks]] that have ground or grass as material. */
    lazy val soilBlocks: Set[Block] = granularBlocks.filter(block =>
        block.material == Material.GROUND || block.material == Material.GRASS)

    /** Blocks from [[granularBlocks]] that have clay or sand (this includes gravel) as material. */
    lazy val sedimentBlocks: Set[Block] = granularBlocks.filter(block =>
        block.material == Material.SAND || block.material == Material.CLAY)

    /** Blocks from the Forge ore dictionary registered as "stone" or "standstone" or any string starting with "ore",
      * as well as the specific blocks hardened clay, stained hardened clay, netherrack, End stone and bedrock. */
    lazy val naturalStoneBlocks: Set[Block] =
        dictionaryBlocks("stone") ++ dictionaryBlocks("sandstone") ++ dictionaryBlocks(_.startsWith("ore")) +
        HARDENED_CLAY + STAINED_HARDENED_CLAY + NETHERRACK + END_STONE + BEDROCK

    /** Returns the Forge ore dictionary blocks registered with the name `key`. */
    def dictionaryBlocks(key: String): Set[Block] = dictionaryBlocksAndData(key).map(_.block)

    /** Returns the Forge ore dictionary blocks matching filter `keyFilter`. */
    def dictionaryBlocks(keyFilter: String => Boolean): Set[Block] = dictionaryBlocksAndData(keyFilter).map(_.block)

    /** Returns the Forge ore dictionary blocks registered with the name `key`, with metadata. */
    def dictionaryBlocksAndData(key: String): Set[BlockAndData] = getOres(key).flatMap(itemStackBlockAndData).toSet

    /** Returns the Forge ore dictionary blocks matching filter `keyFilter`, with metadata. */
    def dictionaryBlocksAndData(keyFilter: String => Boolean): Set[BlockAndData] =
        getOreNames.filter(name => name != null && keyFilter(name)).flatMap(dictionaryBlocksAndData).toSet

    /** Value class for [[net.minecraft.block.Block]]s with utility methods. */
    implicit class BlockValue(val block: Block) extends AnyVal {
        def material = block.getMaterial(block.getDefaultState)
        def isSolid  = material.blocksMovement
        def isLiquid = material.isLiquid
        def isSolidOrLiquid = isSolid || isLiquid
        def isGrass  = block == GRASS
        def isDirt   = block == DIRT
        def isClay   = block == CLAY
        def isSand   = block == SAND
        def isGravel = block == GRAVEL
        def isGranular     =     granularBlocks.contains(block)
        def isSoil         =         soilBlocks.contains(block)
        def isSediment     =     sedimentBlocks.contains(block)
        def isNaturalStone = naturalStoneBlocks.contains(block)
        def isGround = isSoil || isSediment || isNaturalStone
        def isDiscreteObject = block.displayOnCreativeTab != BUILDING_BLOCKS
    }

    /** Returns a human-readable localized metadata-independent name for `block` on a best-effort basis.
      * (Some blocks only have data-specific names such as colored wool or clay; for these we fall back on the ore dictionary or unlocalized name.) */
    def displayName(block: Block): String = {
        if(block == STAINED_HARDENED_CLAY)
            STAINED_HARDENED_CLAY.getLocalizedName
        else {
            val name = block.getUnlocalizedName
            val displayName = block.getLocalizedName
            if(displayName != name + ".name")
                displayName
            else getOreIDs(new ItemStack(block)).headOption match {
                case Some(id) =>
                    val dictionaryKey = getOreName(id)
                    val dictionaryName = s"tile.$dictionaryKey.name"
                    val dictionaryDisplayName = I18n.translateToLocal(dictionaryName)
                    if(dictionaryDisplayName != dictionaryName) dictionaryDisplayName
                    else dictionaryKey
                case None => name.replace("tile.", "")
            }
        }
    }

    /** Value class for a block-and-metadata tuple to allow member access by name. */
    implicit class BlockAndDataValue(val bd: BlockAndData) extends AnyVal {
        def block = bd._1
        def data  = bd._2
    }
}
