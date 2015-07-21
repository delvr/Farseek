package farseek

import java.lang.Package._
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3._
import scala.collection._
import scala.math._
import scala.reflect.ClassTag
import scala.util.Random

/** Miscellaneous utility functions and extensions.
  * @author delvr
  */
package object util {

    val tfcLoaded = getPackage("com.bioxx.tfc") != null

    def isVanillaClass(obj: Any) = obj.getClass.getName.startsWith("net.minecraft.")

    //-----------------------------------------------------------------------------------------------------------------
    // Coordinates
    //-----------------------------------------------------------------------------------------------------------------
    /** 2D coordinates (2-tuple of Ints) */
    type XZ = (Int, Int)

    /** 3D coordinates (3-tuple of Ints) */
    type XYZ = (Int, Int, Int)
    
    /** Value class for [[XZ]] 2D coordinates with utility methods. */
    implicit class XzValue(val xz: XZ) extends AnyVal {

        def x = xz._1
        def z = xz._2

        def y(y: Int): XYZ = (x, y, z)
        def xyz(y: Int): XYZ = (x, y, z)
        def apply(y: Int): XYZ = (x, y, z)

        def + (d: Direction) = (x + d.x, z + d.z)
        def - (d: Direction) = (x - d.x, z - d.z)

        def + (other: XZ) = (x + other.x, z + other.z)
        def - (other: XZ) = (x - other.x, z - other.z)

        def neighbors: Seq[XZ] = Direction.neighbors(x, z)
        def cornerNeighbors: Seq[XZ] = Direction.cornerNeighbors(x, z)
        def allNeighbors: Seq[XZ] = Direction.allNeighbors(x, z)

        def distanceTo(dest: XZ): Double = distance(x, z, dest.x, dest.z)

        def xzFlatArrayIndex(zLength: Int): Int = x*zLength + z
        def zxFlatArrayIndex(xLength: Int): Int = z*xLength + x
    }

    /** Value class for [[XYZ]] 3D coordinates with utility methods. */
    implicit class XyzValue(val xyz: XYZ) extends AnyVal {

        def x = xyz._1
        def y = xyz._2
        def z = xyz._3

        def xz: XZ = (x, z)
        def y(y: Int): XYZ = (x, y, z)
        def apply(y: Int): XYZ = (x, y, z)

        def + (d: Direction) = (x + d.x, y + d.y, z + d.z)
        def - (d: Direction) = (x - d.x, y - d.y, z - d.z)

        def + (dy: Int): XYZ = (x, y + dy, z)
        def - (dy: Int): XYZ = (x, y - dy, z)

        def + (other: XYZ) = (x + other.x, y + other.y, z + other.z)
        def - (other: XYZ) = (x - other.x, y - other.y, z - other.z)

        def < (yOther: Int) = y < yOther
        def > (yOther: Int) = y > yOther

        def <= (yOther: Int) = y <= yOther
        def >= (yOther: Int) = y >= yOther

        def above: XYZ = this + 1
        def below: XYZ = this - 1

        def neighbors: Seq[XYZ] = Direction.neighbors(x, y, z)
        def cornerNeighbors: Seq[XYZ] = Direction.cornerNeighbors(x, y, z)
        def allNeighbors: Seq[XYZ] = Direction.allNeighbors(x, y, z)

        def distanceTo(dest: XYZ): Double = distance(x, y, z, dest.x, dest.y, dest.z)
        def flatDistanceTo(dest: XYZ): Double = distance(x, z, dest.x, dest.z)

        def xzyFlatArrayIndex(zLength: Int, yLength: Int): Int = xz.xzFlatArrayIndex(zLength)*yLength + y
        def zxyFlatArrayIndex(xLength: Int, yLength: Int): Int = xz.zxFlatArrayIndex(xLength)*yLength + y

        def to(yEnd: Int): SeqView[XYZ, Seq[_]] = between(xyz, y(yEnd))
        def to(yEnd: Int, f: XYZ => Boolean): SeqView[XYZ, Seq[_]] = to(yEnd).takeWhile(f)

        def debug = s"tp $x $y $z"
    }

    def between(start: XZ, end: XZ): SeqView[XZ, Seq[_]] = {
        if(start.x == end.x)
            between(start.z, end.z).map((start.x, _))
        else if(start.z == end.z)
            between(start.x, end.x).map((_, start.z))
        else throw new IllegalArgumentException(
            s"Positions $start and $end are not aligned (lack a common coordinate)")
    }

    def between(start: XYZ, end: XYZ): SeqView[XYZ, Seq[_]] = {
        if(start.x == end.x && start.y == end.y)
            between(start.z, end.z).map((start.x, start.y, _))
        else if(start.x == end.x && start.z == end.z)
            between(start.y, end.y).map((start.x, _, start.z))
        else if(start.y == end.y && start.z == end.z)
            between(start.x, end.x).map((_, start.y, start.z))
        else throw new IllegalArgumentException(
            s"Positions $start and $end are not aligned (lack two common coordinates)")
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Arrays
    //-----------------------------------------------------------------------------------------------------------------
    /** Value class for [[Array]]s with utility methods. */
    implicit class ArrayValue[T](val array: Array[T]) extends AnyVal {

        def option: Option[Array[T]] = if(array.length > 0) Some(array) else None

        def optionElement(i: Int): Option[T] = if(hasIndex(i)) Some(array(i)) else None
        
        def hasIndex(i: Int): Boolean = i >= 0 && i < array.length

        def moduloElement(i: Int): T = array(i % array.length)

        def clampedIndex(i: Int): Int = clamped(0, i, array.length - 1)

        def clampedIndexElement(i: Int): T = array(clampedIndex(i))

        def randomElement(implicit random: Random): T = array(random.nextInt(array.length))

        def randomElementOption(implicit random: Random): Option[T] = option.map(_.randomElement)

        def copyTo(dest: Array[T]) { Array.copy(array, 0, dest, 0, min(array.length, dest.length)) }
    }

    def copyOf[T: ClassTag](array: Array[T]): Array[T] = {
        val dest = new Array[T](array.length)
        array.copyTo(dest)
        dest
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Collections
    //-----------------------------------------------------------------------------------------------------------------
    def option[A, C <: Traversable[A]](xs: C with TraversableLike[A, C]): Option[C] =
        if(xs != null && xs.nonEmpty) Some(xs) else None

    def minValues[A, B: Ordering, C <: Traversable[A]](xs: C with TraversableLike[A, C])(f: A => B): C = {
        if(xs.isEmpty) xs else {
            val minValue = xs.minBy(f)
            xs.filter(f(_) == minValue)
        }
    }

    def maxValues[A, B: Ordering, C <: Traversable[A]](xs: C with TraversableLike[A, C])(f: A => B): C = {
        if(xs.isEmpty) xs else {
            val maxValue = xs.maxBy(f)
            xs.filter(f(_) == maxValue)
        }
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Math
    //-----------------------------------------------------------------------------------------------------------------
    def rounded(x: Double): Int = round(x).toInt

    def flooredDivision(a: Int, b: Int): Int = if(a >= 0) a / b else (a - b + 1)/b

    def sqr(x: Double): Double = x * x

    def sqr(x: Int): Int = x * x

    def hypotenuse(a: Double, b: Double): Double = sqrt(sqr(a) + sqr(b))

    def hypotenuse(a: Double, b: Double, c: Double): Double = sqrt(sqr(a) + sqr(b) + sqr(c))

    def distance(x1: Int, z1: Int, x2: Int, z2: Int): Double = hypotenuse(x2 - x1, z2 - z1)

    def distance(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Double = hypotenuse(x2 - x1, y2 - y1, z2 - z1)

    def vector(dx: Double, dy: Double, dz: Double): Vec3 = createVectorHelper(dx, dy, dz)

    def vector(dx: Double, dz: Double): Vec3 = vector(dx, 0d, dz)

    def vector(start: XYZ, end: XYZ): Vec3 = vector(end.x - start.x, end.y - start.y, end.z - start.z)

    def vector(start: XZ, end: XZ): Vec3 = vector(end.x - start.x, end.z - start.z)

    /** Value class for [[Vec3]] 3D vectors with utility methods.
      * @author Vectron himself, in the First Age of Vectron. */
    implicit class Vectron(val vec: Vec3) extends AnyVal {
        def x = vec.xCoord
        def y = vec.yCoord
        def z = vec.zCoord

        def +(other: Vec3): Vec3 = vector(x + other.x, y + other.y, z + other.z)
        def -(other: Vec3): Vec3 = vector(x - other.x, y - other.y, z - other.z)
        def unary_- : Vec3 = vector(-x, -y, -z)
    }

    def curveOffset(start: Double, end: Double, control: Double, t: Double): Double =
        (1-t)*(1-t)*start + (1-t)*t*control*2 + t*t*end

    //-----------------------------------------------------------------------------------------------------------------
    // Random
    //-----------------------------------------------------------------------------------------------------------------
    def halfChance(implicit random: Random) = random.nextBoolean()

    def oneChanceOutOf(n: Int)(implicit random: Random) = random.nextInt(n) == 0

    //-----------------------------------------------------------------------------------------------------------------
    // Text
    //-----------------------------------------------------------------------------------------------------------------
    def textIf(f: Boolean, text: => String) = if(f) text else ""

    def wordWrap(text: Seq[String], columns: Int): Seq[String] =
        text.flatMap(_.split('\n')).flatMap(line => s"(.{1,$columns})($$|\\s)".r.findAllMatchIn(line).map(_.group(1)))

    //-----------------------------------------------------------------------------------------------------------------
    // Misc
    //-----------------------------------------------------------------------------------------------------------------
    def truly(f: => Unit): Boolean = { f; true }

    def unsignedByte(i: Int): Byte = (i & 255).toByte // http://thecodelesscode.com/case/30

    def clamped(min: Int, n: Int, max: Int): Int = if(n < min) min else if (n > max) max else n

    /** Returns a view of a range between `start` and `end`, in reverse order if `start > end`. */
    def between(start: Int, end: Int): SeqView[Int, IndexedSeq[Int]] =
        (if(start <= end) start to end else start to end by -1).view
}
