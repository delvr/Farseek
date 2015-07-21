package farseek

import farseek.block._
import net.minecraft.block.Block
import net.minecraft.block.Block._
import net.minecraft.item._

/**
 * @author delvr
 */
package object item {

    /** Value class for [[Item]]s with utility methods. */
    implicit class ItemValue(val item: Item) extends AnyVal {

        def block: Option[Block] = getBlockFromItem(item).option
    }
}
