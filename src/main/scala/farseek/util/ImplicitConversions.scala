package farseek.util

import farseek.block._
import farseek.config._
import farseek.world._
import farseek.world.gen.{Bounded, BoundingBox}
import net.minecraft.block.Block
import net.minecraft.block.Block._
import net.minecraft.block.state._
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks._
import net.minecraft.item._
import net.minecraft.util.math._
import net.minecraft.world._
import scala.collection.JavaConverters._
import scala.collection._
import scala.language.implicitConversions

/** Miscellaneous implicit conversions. All implicit functions should be centralized here for maintainer convenience since IDEs don't always make it obvious which one is applied
  * (as opposed to methods of implicit value classes, which IDEs can link to directly and can thus be decentralized and placed in their respective application domains).
  * @author delvr
  */
object ImplicitConversions {

    //-----------------------------------------------------------------------------------------------------------------
    // Java types
    //-----------------------------------------------------------------------------------------------------------------
    implicit def scalaIterable[T](collection: java.util.Collection[_]): Iterable[T] = collection.asInstanceOf[java.util.Collection[T]].asScala

    implicit def scalaBuffer[T](list: java.util.List[_]): mutable.Buffer[T] = list.asInstanceOf[java.util.List[T]].asScala

    implicit def scalaMutableSet[T](set: java.util.Set[_]): mutable.Set[T] = set.asInstanceOf[java.util.Set[T]].asScala

    //-----------------------------------------------------------------------------------------------------------------
    // Config settings
    //-----------------------------------------------------------------------------------------------------------------
    implicit def Setting2Value[T](setting: Setting[T]): T = setting.value

    implicit def NumericSetting2Int(setting: NumericSetting): Int = setting.value.toInt

    //-----------------------------------------------------------------------------------------------------------------
    // Coordinates
    //-----------------------------------------------------------------------------------------------------------------
    implicit def xyzBlockPos(xyz: XYZ): BlockPos = new BlockPos(xyz.x, xyz.y, xyz.z)

    implicit def blockPosXyz(blockPos: BlockPos): XYZ = (blockPos.getX, blockPos.getY, blockPos.getZ)

    implicit def xzPosition(xyz: XYZ): XZ = xyz.xz

    implicit def xyBlockPos(xz: XZ): BlockPos = new BlockPos(xz.x, 0, xz.z)

    //-----------------------------------------------------------------------------------------------------------------
    // Blocks
    //-----------------------------------------------------------------------------------------------------------------
    implicit def blockOption(block: Block): Option[Block] = if(block != AIR) Option(block) else None

    implicit def blockAndDataOption(blockAndData: BlockAndData): Option[BlockAndData] = blockOption(blockAndData.block).map(_ -> blockAndData.data)

    implicit def blockStateOption(blockState: IBlockState): Option[IBlockState] = if(blockState != null && blockState.getBlock == AIR) Some(blockState) else None

    implicit def optionBlock(option: Option[Block]): Block = option.getOrElse(AIR)

    implicit def optionBlockAndData(option: Option[BlockAndData]): BlockAndData = option.getOrElse(AIR)

    implicit def optionBlockState(option: Option[IBlockState]): IBlockState = option.getOrElse(AIR)

    implicit def itemBlock(item: Item): Option[Block] = blockOption(getBlockFromItem(item))

    implicit def itemStackBlock(stack: ItemStack): Option[Block] = if(stack == null) None else itemBlock(stack.getItem)

    implicit def itemStackBlockAndData(stack: ItemStack): Option[BlockAndData] = itemStackBlock(stack).map(_ -> stack.getItemDamage)

    implicit def itemStackBlockState(stack: ItemStack): Option[IBlockState] = itemStackBlock(stack).map(_.getStateFromMeta(stack.getMetadata))

    implicit def blockStateBlock(blockState: IBlockState): Block = blockState.getBlock

    implicit def blockStateMetadata(blockState: IBlockState): Int = blockState.getBlock.getMetaFromState(blockState)

    implicit def blockAndZeroData(block: Block): BlockAndData = (block, 0)

    implicit def blockDefaultState(block: Block): IBlockState = block.getDefaultState

    implicit def blockAndDataBlock(blockAndData: BlockAndData): Block = blockAndData.block

    implicit def blockAndDataState(blockAndData: BlockAndData): IBlockState = blockAndData._1.getStateFromMeta(blockAndData._2)

    implicit def blockStateData(blockState: IBlockState): BlockAndData = (blockState.getBlock, blockState.getBlock.getMetaFromState(blockState))

    //-----------------------------------------------------------------------------------------------------------------
    // Worlds
    //-----------------------------------------------------------------------------------------------------------------
    implicit def blockAccessProvider(bac: IBlockAccess): WorldProvider = bac.worldProvider

    //-----------------------------------------------------------------------------------------------------------------
    // Bounding boxes
    //-----------------------------------------------------------------------------------------------------------------
    implicit def boundedBoundingBox(bounded: Bounded): BoundingBox = bounded.boundingBox

    implicit def entityBoundingBox(entity: Entity): AxisAlignedBB = entity.getEntityBoundingBox
}
