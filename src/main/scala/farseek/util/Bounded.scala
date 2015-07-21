package farseek.util

import scala.collection._
import scala.math._

/** Interface for objects defined by min and max x/y/z coordinates.
  * @author delvr
  */
trait Bounded {

    def xMin: Int
    def yMin: Int
    def zMin: Int
    def xMax: Int
    def yMax: Int
    def zMax: Int

    def xs: Range = xMin to xMax
    def ys: Range = yMin to yMax
    def zs: Range = zMin to zMax

    def xSize: Int = xMax - xMin + 1
    def ySize: Int = yMax - yMin + 1
    def zSize: Int = zMax - zMin + 1

    def xCenter: Int = xMin + xSize/2
    def yCenter: Int = yMin + ySize/2
    def zCenter: Int = zMin + zSize/2

    def xzCenter: XZ = (xCenter, zCenter)
    def xyzCenter: XYZ = (xCenter, yCenter, zCenter)

    def clamp(xz: XZ): XZ =
        if(contains(xz)) xz else (clamped(xMin, xz.x, xMax), clamped(zMin, xz.z, zMax))

    def clamp(xyz: XYZ): XYZ =
        if(contains(xyz)) xyz else (clamped(xMin, xyz.x, xMax), clamped(yMin, xyz.y, yMax), clamped(zMin, xyz.z, zMax))

    def contains(xyz: XYZ): Boolean =
        xMin <= xyz.x && yMin <= xyz.y && zMin <= xyz.z && xMax >= xyz.x && yMax >= xyz.y && zMax >= xyz.z

    def contains(other: Bounded): Boolean =
        xMin <= other.xMin && xMax >= other.xMax &&
        yMin <= other.yMin && yMax >= other.yMax &&
        zMin <= other.zMin && zMax >= other.zMax

    def contains(xz: XZ): Boolean = contains(xz(yMin))

    def isWithin(other: Bounded): Boolean = other.contains(this)

    def intersects(other: Bounded): Boolean =
        xMin <= other.xMax && xMax >= other.xMin &&
        yMin <= other.yMax && yMax >= other.yMin &&
        zMin <= other.zMax && zMax >= other.zMin

    def intersectionWith(other: Bounded): BoundingBox = BoundingBox(
        max(xMin, other.xMin), max(yMin, other.yMin), max(zMin, other.zMin),
        min(xMax, other.xMax), min(yMax, other.yMax), min(zMax, other.zMax))

    def xzPerimeter: SeqView[XZ, Seq[_]] =
        between((xMin, zMin), (xMax, zMin)) ++ between((xMax, zMin + 1), (xMax, zMax - 1)) ++
        between((xMax, zMax), (xMin, zMax)) ++ between((xMin, zMax - 1), (xMin, zMin + 1))

    override def toString = getClass.getSimpleName + boundsToString

    def boundsToString = s"[$xMin..$xMax, $yMin..$yMax, $zMin..$zMax]"

    def toDebugString = s"tp $xCenter $yCenter $zCenter"
}

trait Bounds extends Bounded {
    def bounds: Bounded
    def xMin = bounds.xMin
    def yMin = bounds.yMin
    def zMin = bounds.zMin
    def xMax = bounds.xMax
    def yMax = bounds.yMax
    def zMax = bounds.zMax
}

/** Immutable version of the vanilla [[net.minecraft.world.gen.structure.StructureBoundingBox]].
  * @author delvr
  */
case class BoundingBox(xMin: Int, yMin: Int, zMin: Int, xMax: Int, yMax: Int, zMax: Int) extends Bounded {
    override def toString = boundsToString
}

object BoundingBox {

    def sizedBox(xMin: Int, yMin: Int, zMin: Int, xSize: Int, ySize: Int, zSize: Int): BoundingBox =
        BoundingBox(xMin, yMin, zMin, xMin + xSize - 1, yMin + ySize - 1, zMin + zSize - 1)

    def sizedBox(xMin: Int, yMin: Int, zMin: Int, xzSize: Int, ySize: Int): BoundingBox =
        sizedBox(xMin, yMin, zMin, xzSize, ySize, xzSize)
}
