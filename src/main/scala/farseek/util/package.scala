package farseek

import farseek.world.Direction
import java.util.Random
import net.minecraft.util.math.MathHelper._
import net.minecraft.util.math._
import scala.math._
import scala.reflect.ClassTag

/** Miscellaneous utility functions.
  * @author delvr
  */
package object util {

    /** 2D coordinates (2-tuple of Ints) */
    type XZ = (Int, Int)

    /** 3D coordinates (3-tuple of Ints) */
    @deprecated("Use net.minecraft.util.math.BlockPos instead", "2.3")
    type XYZ = (Int, Int, Int)

    def unsupported: Nothing = throw new UnsupportedOperationException

    def above(xyz: XYZ) = xyz.above
    def below(xyz: XYZ) = xyz.below

    def sortedCSV(list: Seq[String]): String = list.sorted.mkString(", ")

    def wordWrap(text: Seq[String], columns: Int): Seq[String] =
        text.flatMap(_.split('\n')).flatMap(line => s"(.{1,$columns})($$|\\s)".r.findAllMatchIn(line).map(_.group(1)))

    def halfChance(implicit random: Random) = random.nextBoolean

    def oneChanceOutOf(n: Int)(implicit random: Random) = random.nextInt(n) == 0

    def randomElement[T](seq: Array[T])(implicit random: Random): T = seq(random.nextInt(seq.length))

    def randomElementOption[T](seq: Array[T])(implicit random: Random): Option[T] = if(seq.nonEmpty) Some(randomElement(seq)) else None

    def clampedIndex(seq: Array[_], i: Int): Int = clamp(i, 0, seq.length - 1)

    def clamped[T](seq: Array[T], i: Int): T = seq(clampedIndex(seq, i))

    def clamped(min: Int, n: Int, max: Int): Int = clamp(n, min, max)

    def isValidIndex(seq: Array[_], i: Int): Boolean = i >= 0 && i < seq.length

    def copyTo[T](src: Array[T], dest: Array[T]): Array[T] = {
        Array.copy(src, 0, dest, 0, min(src.length, dest.length))
        dest
    }

    def copyOf[T: ClassTag](src: Array[T]): Array[T] = copyTo(src, new Array[T](src.length))

    def between(start: Int, end: Int): Range = if(start <= end) start to end else start to end by -1

    def hypotenuse(a: Double, b: Double): Double = math.sqrt(a*a + b*b)

    def hypotenuse(a: Double, b: Double, c: Double): Double = math.sqrt(a*a + b*b + c*c)

    def distance(x1: Int, z1: Int, x2: Int, z2: Int): Double = hypotenuse(x2 - x1, z2 - z1)

    def distance(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Double = hypotenuse(x2 - x1, y2 - y1, z2 - z1)

    def fillSeq[T](times: Int, element: T): Seq[T] = Seq.fill(times)(element) // ensures element is computed only once

    /** Value class for [[XZ]] 2D coordinates with utility methods. */
    implicit class XzValue(val xz: XZ) extends AnyVal {
        def x = xz._1
        def z = xz._2
        def xyz(y: Int): XYZ = (x, y, z)
        def + (other: XZ) = (x + other.x, z + other.z)
        def - (other: XZ) = (x - other.x, z - other.z)
        def neighbors: Seq[XZ] = Direction.neighbors(x, z)
        def distanceTo(dest: XZ): Double = distance(x, 0, z, dest.x, 0, dest.z)
    }

    /** Value class for [[XYZ]] 3D coordinates with utility methods. */
    @deprecated("Use farseek.util.PosValue instead", "2.3")
    implicit class XyzValue(val xyz: XYZ) extends AnyVal {
        def x = xyz._1
        def y = xyz._2
        def z = xyz._3
        def xz: XZ = (x, z)
        def + (dy: Int): XYZ = (x, y + dy, z)
        def - (dy: Int): XYZ = (x, y - dy, z)
        def + (other: XYZ) = (x + other.x, y + other.y, z + other.z)
        def - (other: XYZ) = (x - other.x, y - other.y, z - other.z)
        def < (other: XYZ) = y < other.y
        def > (other: XYZ) = y > other.y
        def >= (other: XYZ) = y >= other.y
        def <= (other: XYZ) = y <= other.y
        def above: XYZ = this + 1
        def below: XYZ = this - 1
        def       neighbors: Seq[XYZ] = Direction.neighbors(x, y, z)
        def cornerNeighbors: Seq[XYZ] = Direction.cornerNeighbors(x, y, z)
        def    allNeighbors: Seq[XYZ] = Direction.allNeighbors(x, y, z)
        def distanceTo(dest: XYZ): Double = distance(x, y, z, dest.x, dest.y, dest.z)
        def debug = s"tp $x $y $z"
    }

    /** Value class for [[BlockPos]] 3D coordinates with utility methods. */
    implicit class PosValue(val pos: BlockPos) extends AnyVal {
        def x = pos.getX
        def y = pos.getY
        def z = pos.getZ
        def xz: XZ = (x, z)
        def + (dy: Int): BlockPos = pos.up(dy)
        def - (dy: Int): BlockPos = pos.down(dy)
        def + (other: BlockPos) = pos.add(other)
        def - (other: BlockPos) = pos.subtract(other)
        def < (other: BlockPos) = y < other.y
        def > (other: BlockPos) = y > other.y
        def >= (other: BlockPos) = y >= other.y
        def <= (other: BlockPos) = y <= other.y
        def above: BlockPos = pos.up
        def below: BlockPos = pos.down
        def       neighbors: Seq[BlockPos] = Direction.neighbors(pos)
        def cornerNeighbors: Seq[BlockPos] = Direction.cornerNeighbors(pos)
        def    allNeighbors: Seq[BlockPos] = Direction.allNeighbors(pos)
        def distanceTo(dest: BlockPos): Double = pos.distanceSq(dest)
        def debug = s"tp $x $y $z"
    }

    /** Value class for [[Vec3d]] 3D vectors with utility methods.
      * @author Vectron himself, in the First Age of Vectron. */
    implicit class Vectron(val vec: Vec3d) extends AnyVal {
        def +(other: Vec3d) = vec.add(other)
        def -(other: Vec3d) = vec.subtract(other)
        def praise() { println("PRAISE VECTRON") }
    }
}
