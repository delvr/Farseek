package farseek.world

import farseek.util._
import net.minecraft.util.math.BlockPos

/** A directional offset in the X/Y/Z axes.
  * @author delvr
  */
case class Direction(x: Int, y: Int, z: Int) {

    def +(other: Direction) = Direction(x + other.x, y + other.y, z + other.z)

    def opposite = Direction(-x, -y, -z)

    override val toString = {
        val s = new StringBuilder
        if(y < 0) s ++= "Down " else if(y > 0) s ++= "Up "
        if(z < 0) s ++= "North" else if(z > 0) s ++= "South"
        if(x < 0) s ++= "West"  else if(x > 0) s ++= "East"
        s.toString()
    }
}

/** Direction-related constants and utilities.
  * @author delvr
  */
object Direction {

    def apply(dx: Int, dz: Int): Direction = Direction(dx, 0, dz)

    val Up   = Direction(0,  1, 0)
    val Down = Direction(0, -1, 0)
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

    val CardinalDirections3d = VerticalDirections ++ CardinalDirections
    val CornerDirections3d = OrdinalDirections.map(Up + _) ++ OrdinalDirections.map(Down + _)
    val AllDirections3d = CardinalDirections3d ++ CornerDirections3d

    def neighbor(x: Int,         z: Int, d: Direction): XZ  = (x + d.x,          z + d.z)
    def neighbor(x: Int, y: Int, z: Int, d: Direction): XYZ = (x + d.x, y + d.y, z + d.z)
    def neighbor(pos: BlockPos, d: Direction): BlockPos = pos.add(d.x, d.y, d.z)

    def       neighbors(x: Int, z: Int): Seq[XZ] = CardinalDirections.map(neighbor(x, z, _))
    def cornerNeighbors(x: Int, z: Int): Seq[XZ] =  OrdinalDirections.map(neighbor(x, z, _))
    def    allNeighbors(x: Int, z: Int): Seq[XZ] =  CompassDirections.map(neighbor(x, z, _))
    
    def       neighbors(x: Int, y: Int, z: Int): Seq[XYZ] = CardinalDirections.map(neighbor(x, y, z, _))
    def cornerNeighbors(x: Int, y: Int, z: Int): Seq[XYZ] =  OrdinalDirections.map(neighbor(x, y, z, _))
    def    allNeighbors(x: Int, y: Int, z: Int): Seq[XYZ] =  CompassDirections.map(neighbor(x, y, z, _))

    def       neighbors(pos: BlockPos): Seq[BlockPos] = CardinalDirections.map(neighbor(pos, _))
    def cornerNeighbors(pos: BlockPos): Seq[BlockPos] =  OrdinalDirections.map(neighbor(pos, _))
    def    allNeighbors(pos: BlockPos): Seq[BlockPos] =  CompassDirections.map(neighbor(pos, _))
}
