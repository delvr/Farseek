package farseek.util

import farseek.util.Direction._
import net.minecraft.util.Vec3
import scala.math._

/** A directional offset in the X/Y/Z axes. Like a vector but with integer components and undefined magnitude.
  * @author delvr
  */
case class Direction(x: Int, y: Int, z: Int) {

    val vector: Vec3 = farseek.util.vector(x, y, z).normalize

    override val toString = {
        val s = new StringBuilder
        for(i <- z to -2) s ++= "North-"
        for(i <- 2 to  z) s ++= "South-"
        for(i <- x to -2) s ++= "West-"
        for(i <- 2 to  x) s ++= "East-"
        if(z < 0) s ++= "North" else if(z > 0) s ++= "South"
        if(x < 0) s ++= "West"  else if(x > 0) s ++= "East"
        if(y < 0 || y > 0) s ++= " "
        if(y < -1 || y > 1) s ++= abs(y) + "-"
        if(y < 0) s ++= "Below" else if(y > 0) s ++= "Above"
        if(s.nonEmpty) s.toString() else "Center"
    }

    def +(other: Direction) = Direction(x + other.x, y + other.y, z + other.z)
    def -(other: Direction) = Direction(x - other.x, y - other.y, z - other.z)

    def unary_- = Direction(-x, -y, -z)

    def right: Direction = this match {
        case North => East
        case South => West
        case West => North
        case East => South
    }

    def left: Direction = this match {
        case North => West
        case South => East
        case West => South
        case East => North
    }

    def normalized(mainComponentLength: Int = 1): Direction = {
        require(mainComponentLength > 0)
        val dMax: Double = max(max(abs(x), abs(z)), abs(y)).toDouble / mainComponentLength.toDouble
        if(dMax == 0) this else Direction(rounded(x / dMax), rounded(y / dMax), rounded(z / dMax))
    }
}

/** Direction-related constants and utilities.
  * @author delvr
  */
object Direction {

    def apply(): Direction = Direction(0, 0, 0)

    def apply(dy: Int): Direction = Direction(0, dy, 0)

    def apply(dx: Int, dz: Int): Direction = Direction(dx, 0, dz)

    def apply(src: XZ, dest: XZ): Direction = Direction(dest.x - src.x, dest.z - src.z)

    def apply(src: XYZ, dest: XYZ): Direction = Direction(dest.x - src.x, dest.y - src.y, dest.z - src.z)

    val Center = Direction()

    val Up   = Direction( 1)
    val Down = Direction(-1)
    val VerticalDirections = Seq(Up, Down)

    val North = Direction( 0, -1)
    val South = Direction( 0,  1)
    val East  = Direction( 1,  0)
    val West  = Direction(-1,  0)
    val CardinalDirections = Seq(North, South, East, West)

    val NorthEast = North + East
    val NorthWest = North + West
    val SouthEast = South + East
    val SouthWest = South + West
    val OrdinalDirections = Seq(NorthEast, NorthWest, SouthEast, SouthWest)

    val CompassDirections = CardinalDirections ++ OrdinalDirections

    def       neighbors(xz: XZ): Seq[XZ] = CardinalDirections.map(xz + _)
    def cornerNeighbors(xz: XZ): Seq[XZ] =  OrdinalDirections.map(xz + _)
    def    allNeighbors(xz: XZ): Seq[XZ] =  CompassDirections.map(xz + _)
    
    def       neighbors(xyz: XYZ): Seq[XYZ] = CardinalDirections.map(xyz + _)
    def cornerNeighbors(xyz: XYZ): Seq[XYZ] =  OrdinalDirections.map(xyz + _)
    def    allNeighbors(xyz: XYZ): Seq[XYZ] =  CompassDirections.map(xyz + _)
}
