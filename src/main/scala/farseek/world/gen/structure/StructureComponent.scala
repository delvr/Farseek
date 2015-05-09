package farseek.world.gen.structure

import farseek.util.ImplicitConversions._
import farseek.util._
import farseek.world.BlockWriteAccess._
import farseek.world._
import farseek.world.gen.{Bounded, _}
import java.util.Random

/** Farseek implementation of world generation [[Structure]] components, as an alternative to vanilla [[StructureComponent]]s.
  *
  * @migration(message = "Structure API is not fully stable and will change for Streams version 0.2", version = "1.1.0")
  * @author delvr
  */
abstract class StructureComponent(structure: Structure[_]) extends Bounded with Logging {

    val paddedBox: BoundingBox = boundingBox

    def isValid = paddedBox.isWithin(structure) && structure.intersectingComponents(this, _.boundingBox).isEmpty

    def build(area: PopulatingArea, random: Random) { build(new ComponentBuilder(this, area), random) }

    protected def build(implicit blockSetter: BlockSetter, random: Random)

    override def toString = if(paddedBox == boundingBox) super.toString else s"${super.toString} $paddedBox"
}

/** A [[WrappedBlockSetter]] for building a [[StructureComponent]]s. Coordinates are valid if they are within the component's `paddedBox`
  * _and_ within the wrapped [[PopulatingArea]]'s `boundingBox`.
  * @author delvr
  */
class ComponentBuilder(component: StructureComponent, protected val wrapped: PopulatingArea) extends WrappedBlockSetter[BlockSetter] {

    protected val wrappedWriter = NonWorldBlockWriteAccess

    def validAt(xyz: XYZ) = component.paddedBox.contains(xyz) && wrapped.boundingBox.contains(xyz)

    @deprecated(message = "Use validAt(XYZ)", since = "1.0.7")
    def validAt(xz: XZ) = component.paddedBox.contains(xz) && wrapped.boundingBox.contains(xz)
}
