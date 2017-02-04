package farseek.world.gen

import farseek.util._
import farseek.world.BlockAccess

/** Convenience base trait for objects that have a [[BoundingBox]].
  * @author delvr
  */
trait Bounded {

    val boundingBox: BoundingBox

    def xMin = boundingBox.minX
    def yMin = boundingBox.minY
    def zMin = boundingBox.minZ
    def xMax = boundingBox.maxX
    def yMax = boundingBox.maxY
    def zMax = boundingBox.maxZ

    def xCenter = xMin + (xMax - xMin + 1) / 2
    def yCenter = yMin + (yMax - yMin + 1) / 2
    def zCenter = zMin + (zMax - zMin + 1) / 2

    override def toString = s"${getClass.getSimpleName} $boundingBox"

    def debug = boundingBox.debug
}

/** A [[BlockAccess]] that validates coordinates against a [[BoundingBox]].
  * @author delvr
  */
trait BoundedBlockAccess extends BlockAccess with Bounded {

    def validAt(xyz: XYZ) = boundingBox.contains(xyz)

    @deprecated(message = "Use validAt(XYZ)", since = "1.0.7")
    def validAt(xz: XZ) = validAt(xz.xyz(yMin))
}
