package farseek.world

import farseek.util._
import farseek.world.Direction._

/** A system for translating X/Y/Z local coordinates into world coordinates, and vice-versa.
  * @author delvr
  */
@deprecated("Use farseek.world.IBlockAccessOps with absolute coordinates instead", "2.3")
trait CoordinateSystem {

    def xWorld(x: Int, z: Int): Int
    def yWorld(    y: Int    ): Int
    def zWorld(x: Int, z: Int): Int
    def xzWorld(x: Int, z: Int): XZ = (xWorld(x, z), zWorld(x, z))
    def xzWorld(xz: XZ): XZ = xzWorld(xz.x, xz.z)
    def xyzWorld(x: Int, y: Int, z: Int): XYZ = (xWorld(x, z), yWorld(y), zWorld(x, z))
    def xyzWorld(xyz: XYZ): XYZ = xyzWorld(xyz.x, xyz.y, xyz.z)

    def xLocal(x: Int, z: Int): Int
    def yLocal(    y: Int    ): Int
    def zLocal(x: Int, z: Int): Int
    def xzLocal(x: Int, z: Int): XZ = (xLocal(x, z), zLocal(x, z))
    def xzLocal(xz: XZ): XZ = xzLocal(xz.x, xz.z)
    def xyzLocal(x: Int, y: Int, z: Int): XYZ = (xLocal(x, z), yLocal(y), zLocal(x, z))
    def xyzLocal(xyz: XYZ): XYZ = xyzLocal(xyz.x, xyz.y, xyz.z)
}

/** Identity coordinates where local and world coordinates are the same.
  * @author delvr
  */
@deprecated("Use farseek.world.IBlockAccessOps with absolute coordinates instead", "2.3")
object AbsoluteCoordinates extends CoordinateSystem {

    def xWorld(x: Int, z: Int) = x
    def yWorld(    y: Int    ) = y
    def zWorld(x: Int, z: Int) = z

    def xLocal(x: Int, z: Int) = x
    def yLocal(    y: Int    ) = y
    def zLocal(x: Int, z: Int) = z
}

/** A coordinate system where local coordinates are shifted by `xWorldMin/yWorldMin/zWorldMin`.
  * @author delvr
  */
@deprecated("Use farseek.world.IBlockAccessOps with absolute coordinates instead", "2.3")
class RelativeCoordinates(xWorldMin: Int, yWorldMin: Int, zWorldMin: Int) extends CoordinateSystem {

    def xWorld(x: Int, z: Int) = xWorldMin + x
    def yWorld(    y: Int    ) = yWorldMin + y
    def zWorld(x: Int, z: Int) = zWorldMin + z

    def xLocal(x: Int, z: Int) = x - xWorldMin
    def yLocal(    y: Int    ) = y - yWorldMin
    def zLocal(x: Int, z: Int) = z - zWorldMin
}

/** Relative coordinates where local coordinates are shifted and rotated/mirrored in a certain X or Z direction. The
  * translation rules are the same as those used in vanilla Minecraft structures such as villages and strongholds.
  * @author delvr
  */
@deprecated("Use farseek.world.IBlockAccessOps with absolute coordinates instead", "2.3")
class DirectedCoordinates(xWorldMin: Int, yWorldMin: Int, zWorldMin: Int, zLocalMax: Int, orientation: Direction)
        extends RelativeCoordinates(xWorldMin, yWorldMin, zWorldMin) {

    require(zLocalMax >= 0)
    require(CardinalDirections.contains(orientation))
    assert {
        val xyz = xyzWorld(0, 0, 0)
        xyzLocal(xyz.x, xyz.y, xyz.z) == (0, 0, 0)
    }

    override def xWorld(x: Int, z: Int) = xWorldMin + (orientation match {
        case South | North => x
        case East => z
        case West => zLocalMax - z
    })

    override def zWorld(x: Int, z: Int) = zWorldMin + (orientation match {
        case East | West => x
        case South => z
        case North => zLocalMax - z
    })

    override def xLocal(x: Int, z: Int) = orientation match {
        case South | North => x - xWorldMin
        case East  | West  => z - zWorldMin
    }

    override def zLocal(x: Int, z: Int) = orientation match {
        case South =>              z - zWorldMin
        case North => zLocalMax - (z - zWorldMin)
        case East  =>              x - xWorldMin
        case West  => zLocalMax - (x - xWorldMin)
    }
}