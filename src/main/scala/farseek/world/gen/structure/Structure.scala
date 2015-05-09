package farseek.world.gen.structure

import farseek.util.ImplicitConversions._
import farseek.util._
import farseek.world.gen._
import java.util.Random
import net.minecraft.world._
import net.minecraft.world.gen.structure._
import scala.collection.mutable

/** Farseek implementation of world generation structures, as an alternative to vanilla [[StructureStart]]s.
  *
  * @migration(message = "Structure API is not fully stable and will change for Streams version 0.2", version = "1.1.0")
  * @author delvr
  */
abstract class Structure[T <: StructureComponent](generator: StructureGenerator[_], val boundingBox: BoundingBox,
                                                  protected val worldProvider: WorldProvider) extends Bounded with Logging {
    
    protected val components = mutable.Buffer[T]()

    def generate(implicit worldAccess: IBlockAccess, random: Random)

    def +=(component: T) {
        components += component
    }

    def isValid: Boolean

    def commit() {
        boundingBox.clear()
        components.foreach(component => boundingBox.expandTo(component.paddedBox))
    }

    def clear() {
        components.clear()
    }

    def build(area: PopulatingArea, random: Random) {
        require(area.worldProvider == this.worldProvider)
        intersectingComponents(area, _.paddedBox).foreach(_.build(area, random))
    }

    def intersectingComponents(area: BoundingBox, componentBox: StructureComponent => BoundingBox): Seq[T] =
        if(boundingBox.intersectsWith(area)) components.filter(componentBox(_).intersectsWith(area)) else Seq.empty

    def intersectingComponentsAt(xz: XZ, componentBox: StructureComponent => BoundingBox): Seq[T] =
        if(boundingBox.contains(xz)) components.filter(componentBox(_).contains(xz)) else Seq.empty

    override def toString = s"${super.toString}: ${components.mkString(" ")}"

    override def debug = s"${super.toString} ${super.debug}: ${components.map(_.debug).mkString("  ")}"
}
