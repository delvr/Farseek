package farseek.util

import farseek.block.{BlockAndData, _}
import farseek.config._
import farseek.item._
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item._
import net.minecraft.world._
import net.minecraft.world.storage.WorldInfo
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.language.implicitConversions

/** Miscellaneous implicit conversions. All implicit functions should be centralized here for maintainer convenience
  * since IDEs don't always make it obvious which one is applied (as opposed to methods of implicit value classes,
  * which IDEs can link to directly and can thus be decentralized and placed in their respective application domains).
  * @author delvr
  */
object ImplicitConversions {

    //-----------------------------------------------------------------------------------------------------------------
    // Coordinates
    //-----------------------------------------------------------------------------------------------------------------
    implicit def xzPosition(xyz: XYZ): XZ = xyz.xz

    //-----------------------------------------------------------------------------------------------------------------
    // Blocks & items
    //-----------------------------------------------------------------------------------------------------------------
    implicit def blockAndZeroData(block: Block): BlockAndData = (block, 0)

    implicit def blockAndDataBlock(blockAndData: BlockAndData): Block = blockAndData.block

    implicit def blockAndDataData(blockAndData: BlockAndData): Int = blockAndData.data

    implicit def itemStackItem(stack: ItemStack): Item = stack.getItem

    implicit def itemStackBlock(stack: ItemStack): Option[Block] = stack.getItem.block

    implicit def itemStackBlockAndData(stack: ItemStack): Option[BlockAndData] =
        itemStackBlock(stack).map(_ -> stack.getItemDamage)

    implicit def material(block: Block): Material = block.material

    //-----------------------------------------------------------------------------------------------------------------
    // Worlds & biomes
    //-----------------------------------------------------------------------------------------------------------------
    implicit def worldInfo(world: World): WorldInfo = world.getWorldInfo

    implicit def worldType(world: World): WorldType = world.getTerrainType

    implicit def worldProvider(world: World): WorldProvider = world.provider

    //-----------------------------------------------------------------------------------------------------------------
    // Config settings
    //-----------------------------------------------------------------------------------------------------------------
    implicit def settingValue[T](setting: ConfigSetting[T]): T = setting.value

    implicit def numericSettingIntValue(setting: NumericSetting): Int = setting.value.toInt

    implicit def customChoiceValue[T](choice: CustomChoice[T]): T = choice.value

    implicit def simpleCustomChoiceValue[T](choice: SimpleCustomChoice[T]): T = choice.value

    //-----------------------------------------------------------------------------------------------------------------
    // Java types
    //-----------------------------------------------------------------------------------------------------------------
    implicit def scalaIterable[T](collection: java.util.Collection[_]): Iterable[T] =
        collection.asInstanceOf[java.util.Collection[T]].asScala

    implicit def scalaBuffer[T](list: java.util.List[_]): mutable.Buffer[T] =
        list.asInstanceOf[java.util.List[T]].asScala

    implicit def scalaMutableSet[T](set: java.util.Set[_]): mutable.Set[T] =
        set.asInstanceOf[java.util.Set[T]].asScala
}
