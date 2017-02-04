package farseek.world

import farseek.util.ImplicitConversions._
import farseek.util.Reflection._
import farseek.util.{XYZ, _}
import farseek.world.Direction._
import java.lang.reflect.Field
import java.util.Random
import net.minecraft.world._
import net.minecraft.world.chunk._
import net.minecraft.world.gen.structure.StructureBoundingBox
import scala.collection.mutable

/** World generation utilities.
  * @author delvr
  */
package object gen {

    type BoundingBox = StructureBoundingBox

    val ChunkSize = 16
    val ChunkArea = ChunkSize * ChunkSize
    val iChunkMax = ChunkSize - 1

    /** Maps [[IChunkGenerator]]s with the first field of [[World]] type or subtype declared in their class. */
    val chunkGeneratorWorldClassFields = mutable.Map[Class[_ <: IChunkGenerator], Field]().withDefault(classFields[World](_).head)

    def chunkRandom(xChunk: Int, zChunk: Int)(implicit w: WorldProvider): Random = {
        val worldSeed = w.getSeed
        val random = new Random(worldSeed)
        val xRandom = random.nextLong
        val zRandom = random.nextLong
        random.setSeed((xChunk * xRandom) ^ (zChunk * zRandom) ^ worldSeed)
        random
    }

    def sizedBox(x: Int, z: Int, size: Int)(implicit w: IBlockAccess): BoundingBox = sizedBox(x, z, size, size)

    def sizedBox(xMin: Int, zMin: Int, xSize: Int, zSize: Int)(implicit w: IBlockAccess): BoundingBox =
        sizedBox(xMin, 0, zMin, xSize, w.height, zSize)

    def sizedBox(xMin: Int, yMin: Int, zMin: Int, xSize: Int, ySize: Int, zSize: Int): BoundingBox =
        new BoundingBox(xMin, yMin, zMin, xMin + xSize - 1, yMin + ySize - 1, zMin + zSize - 1)

    def worldHeightBox(xMin: Int, zMin: Int, xMax: Int, zMax: Int)(implicit w: IBlockAccess): BoundingBox =
        new BoundingBox(xMin, 0, zMin, xMax, w.yMax, zMax)

    /** Value class for [[BoundingBox]]es with utility methods. */
    implicit class BoundingBoxValue(val box: BoundingBox) extends AnyVal {
        def xMin = box.minX
        def yMin = box.minY
        def zMin = box.minZ
        def xMax = box.maxX
        def yMax = box.maxY
        def zMax = box.maxZ

        def xCenter = xMin + (xMax - xMin + 1) / 2
        def yCenter = yMin + (yMax - yMin + 1) / 2
        def zCenter = zMin + (zMax - zMin + 1) / 2

        def xs = xMin to xMax
        def ys = yMin to yMax
        def zs = zMin to zMax

        def isValid = xMin <= xMax && yMin <= yMax && zMin <= zMax

        def isWithin(otherBox: BoundingBox) =
            xMin >= otherBox.xMin && xMax <= otherBox.xMax &&
            yMin >= otherBox.yMin && yMax <= otherBox.yMax &&
            zMin >= otherBox.zMin && zMax <= otherBox.zMax

        def contains(x: Int, z: Int) = box.isVecInside(x, yMin, z)
        def contains(x: Int, y: Int, z: Int) = box.isVecInside(x, y, z)
        def contains(xz: XZ): Boolean = contains(xz.xyz(yMin))
        def contains(xyz: XYZ): Boolean = box.isVecInside(xyz)
        def contains(otherBox: StructureBoundingBox) = otherBox.isWithin(box)

        def intersects(chunk: Chunk) = intersectsChunkAt(chunk.xPosition, chunk.zPosition)

        def intersectsChunkAt(xChunk: Int, zChunk: Int) = box.intersectsWith(xChunk*ChunkSize, zChunk*ChunkSize, xChunk*ChunkSize + iChunkMax, zChunk*ChunkSize + iChunkMax)

        def clear() {
            box.minX = Int.MaxValue
            box.minY = Int.MaxValue
            box.minZ = Int.MaxValue
            box.maxX = Int.MinValue
            box.maxY = Int.MinValue
            box.maxZ = Int.MinValue
        }

        def adjacentBox(direction: Direction, xSize: Int, ySize: Int, zSize: Int, hOffset: Int = 0, vOffset: Int = 0): BoundingBox = {
            require(CardinalDirections.contains(direction))
            val newBox = direction match {
                case South => sizedBox(xMin + hOffset, yMin + vOffset, zMax + 1      , xSize, ySize, zSize)
                case North => sizedBox(xMin + hOffset, yMin + vOffset, zMin - zSize  , xSize, ySize, zSize)
                case East  => sizedBox(xMax + 1      , yMin + vOffset, zMin + hOffset, xSize, ySize, zSize)
                case West  => sizedBox(xMin - xSize  , yMin + vOffset, zMin + hOffset, xSize, ySize, zSize)
            }
            assert(!newBox.intersectsWith(box))
            newBox
        }

        def debug = s"tp $xCenter $yCenter $zCenter"
    }
}
