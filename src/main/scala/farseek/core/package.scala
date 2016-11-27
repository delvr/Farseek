package farseek

import java.io.File
import scala.language.reflectiveCalls

package object core {

  final val FMLDeobftweakerSortIndex = 1000

  var gameDir: File = _
  var isDev: Boolean = _

  lazy val spongeLoaded = try {
    Class.forName("org.spongepowered.common.interfaces.world.IMixinWorldServer")
    true
  } catch { case e: ClassNotFoundException => false }

  final def allFiles(path: File): Array[File] =
    if(path == null || !path.exists || !path.canRead) Array()
    else if(path.isFile) Array(path)
    else if(path.isDirectory) path.listFiles.flatMap(allFiles)
    else Array()

  def using[T <: { def close(): Unit }, R](resource: T)(f: T => R): R =
    try f(resource) finally if(resource != null) resource.close()

  def internalName(name: String) = name.replace('.', '/')
}
