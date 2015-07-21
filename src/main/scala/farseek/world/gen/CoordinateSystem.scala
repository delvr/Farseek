package farseek.world.gen

import farseek.util.Direction._
import farseek.util._

/** A system for translating X/Y/Z local coordinates into world coordinates, and vice-versa.
  * @author delvr
  */
trait CoordinateSystem {

    def xWorld(xz: XZ): Int
    def yWorld(y: Int): Int
    def zWorld(xz: XZ): Int
    def xzWorld(xz: XZ): XZ = (xWorld(xz), zWorld(xz))
    def xyzWorld(xyz: XYZ): XYZ = (xWorld(xyz.xz), yWorld(xyz.y), zWorld(xyz.xz))

    def xLocal(xz: XZ): Int
    def yLocal(y: Int): Int
    def zLocal(xz: XZ): Int
    def xzLocal(xz: XZ): XZ = (xLocal(xz), zLocal(xz))
    def xyzLocal(xyz: XYZ): XYZ = (xLocal(xyz.xz), yLocal(xyz.y), zLocal(xyz.xz))
}

/** Shorthand synonyms for coordinate system names.
  * @author delvr
  */
object CoordinateSystem {
    type CS = CoordinateSystem
    val Abs = AbsoluteCoordinates
}

/** Identity coordinates where local and world coordinates are the same.
  * @author delvr
  */
object AbsoluteCoordinates extends CoordinateSystem {

    def xWorld(xz: XZ) = xz.x
    def yWorld(y: Int) =    y
    def zWorld(xz: XZ) = xz.z

    def xLocal(xz: XZ) = xz.x
    def yLocal(y: Int) =    y
    def zLocal(xz: XZ) = xz.z
}

/** A coordinate system where local coordinates are shifted by `xWorldMin/yWorldMin/zWorldMin`.
  * @author delvr
  */
class RelativeCoordinates(xWorldMin: Int, yWorldMin: Int, zWorldMin: Int) extends CoordinateSystem {

    def xWorld(xz: XZ) = xWorldMin + xz.x
    def yWorld(y: Int) = yWorldMin +    y
    def zWorld(xz: XZ) = zWorldMin + xz.z

    def xLocal(xz: XZ) = xz.x - xWorldMin
    def yLocal(y: Int) =    y - yWorldMin
    def zLocal(xz: XZ) = xz.z - zWorldMin
}

/** Relative coordinates where local coordinates are shifted and rotated/mirrored in a certain X or Z direction. The
  * translation rules are the same as those used in vanilla Minecraft structures such as villages and strongholds.
  * @author delvr
  */
class DirectedCoordinates(xWorldMin: Int, yWorldMin: Int, zWorldMin: Int, zLocalMax: Int, orientation: Direction)
        extends RelativeCoordinates(xWorldMin, yWorldMin, zWorldMin) {

    require(zLocalMax >= 0)
    require(CardinalDirections.contains(orientation))
    assert(xyzLocal(xyzWorld(0, 0, 0)) == (0, 0, 0))

    override def xWorld(xz: XZ) = xWorldMin + (orientation match {
        case South | North => xz.x
        case East => xz.z
        case West => zLocalMax - xz.z
    })

    override def zWorld(xz: XZ) = zWorldMin + (orientation match {
        case East | West => xz.x
        case South => xz.z
        case North => zLocalMax - xz.z
    })

    override def xLocal(xz: XZ) = orientation match {
        case South | North => xz.x - xWorldMin
        case East  | West  => xz.z - zWorldMin
    }

    override def zLocal(xz: XZ) = orientation match {
        case South =>              xz.z - zWorldMin
        case North => zLocalMax - (xz.z - zWorldMin)
        case East  =>              xz.x - xWorldMin
        case West  => zLocalMax - (xz.x - xWorldMin)
    }
}