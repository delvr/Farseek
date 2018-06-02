package farseek.world

import farseek.util._
import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/** Defines the operations supported by a member of the "writable block access" type class, such as a [[World]] or [[BlockSetter]].
  * Using a type class allows us to include vanilla Minecraft [[World]] objects in a consistent interface that they don't originally extend.
  * @see [[http://ropas.snu.ac.kr/~bruno/papers/TypeClasses.pdf Type Classes as Objects and Implicits]]
  * @author delvr
  */
@deprecated("Use farseek.world.WorldOps instead", "2.3")
trait BlockWriteAccess[T]  {

    def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true)(implicit t: T): Boolean

    def setTileEntityAt(xyz: XYZ, entity: TileEntity)(implicit t: T): Boolean
}

/** Top-level container for implicit objects that belong to the [[BlockWriteAccess]] type class.
  * @author delvr
  */
@deprecated("Use farseek.world.WorldOps instead", "2.3")
object BlockWriteAccess {

    /** [[BlockWriteAccess]] implementation for [[World]]s.
      * @author delvr
      */
    @deprecated("Use farseek.world.WorldOps instead", "2.3")
    implicit object WorldBlockAccess extends BlockWriteAccess[World] {

        def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true)(implicit w: World) =
            w.setBlockAt(xyz, block, data, notifyNeighbors)

        def setTileEntityAt(xyz: XYZ, entity: TileEntity)(implicit w: World) = {
            w.setTileEntityAt(xyz, entity)
        }
    }

    /** [[BlockWriteAccess]] implementation for [[BlockSetter]] implementations.
      * @author delvr
      */
    @deprecated("Use farseek.world.WorldOps instead", "2.3")
    implicit object NonWorldBlockWriteAccess extends BlockWriteAccess[BlockSetter] {

        def setBlockAt(xyz: XYZ, block: Block, data: Int = 0, notifyNeighbors: Boolean = true)(implicit w: BlockSetter) =
            w.setBlockAt(xyz, block, data, notifyNeighbors)

        def setTileEntityAt(xyz: XYZ, entity: TileEntity)(implicit w: BlockSetter) =
            w.setTileEntityAt(xyz, entity)
    }
}
