package farseek.core

import farseek.core.MethodReplacements.replacements
import farseek.util.Logging
import java.io._
import java.net.URL
import java.util.zip.ZipException
import java.util.zip.ZipFile
import net.minecraft.launchwrapper._
import net.minecraftforge.common.ForgeVersion._
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex
import net.minecraftforge.fml.relauncher._
import scala.collection.JavaConversions._
import scala.io.Source._
import scala.language.reflectiveCalls

/** Core mod class for Farseek that allows method replacements.
  * @see [[farseek.FarseekMod]] for non-core mod class.
  *
  * Client mods can list method replacements in META-INF/farseek_cm.cfg. The format is as follows:
  *
  * `replacementMethodClass replacedMethodClass devMethodName obfuscatedMethodName methodArguments`
  *
  * For example:
  *
  * `repose.block.FallingBlockExtensions net.minecraft.block.Block onBlockAdded func_176213_c (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V`
  *
  * This will redirect all method calls of `net.minecraft.block.Block.onBlockAdded(World, BlockPos, IBlockState)` to
  * `repose.block.FallingBlockExtensions.onBlockAdded(Block, World, BlockPos, IBlockState)`.
  *
  * Note that the replacement method has the same name and parameters as the original one but is static (on a Scala Object) and
  * has the method owner as an additional initial parameter if the original method was an instance method.
  * Method calls originating from within the replacement method will NOT be redirected, which allows calling the original behavior as needed.
  *
  * Limitations:
  *  - Methods must be replaced in the class where they are defined. If you want to change behavior only for a subclass, you still need to replace the top-level method and then match on the first parameter to handle the subtype.
  *  - No redirection will occur for calls targeting an [[https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html anonymous subclass]] of the redirected method.
  *
  * @author delvr
  */
@SortingIndex(value = FMLDeobftweakerSortIndex + 100) // So we get deobfuscated method call arguments
class FarseekCoreMod extends IFMLLoadingPlugin {

  Launch.classLoader.addTransformerExclusion(getClass.getPackage.getName)
  Launch.classLoader.addTransformerExclusion("farseek.util.Logging")
  Launch.classLoader.addTransformerExclusion("scala")

  val getSetupClass = null
  val getModContainerClass = null
  val getAccessTransformerClass = null
  val getASMTransformerClass =
    if(Package.getPackage("org.spongepowered") != null) Array(classOf[FarseekClassTransformer].getName, classOf[FarseekSpongeClassTransformer].getName)
    else Array(classOf[FarseekClassTransformer].getName)

  def injectData(data: java.util.Map[String, AnyRef]) = {
    gameDir = new File(data("mcLocation").toString)
    isDev = !data("runtimeDeobfuscationEnabled").toString.toBoolean
  }
}

class FarseekClassTransformer extends IClassTransformer with Logging {

  private var checkedSponge = false

  def transform(obfuscatedName: String, deobfuscatedName: String, bytecode: Array[Byte]): Array[Byte] = {
    moveAfterSponge()
    if(bytecode == null) null
    else new FarseekClassVisitor(bytecode, internalName(deobfuscatedName), replacements).patch
  }

  /** Sponge mixins can be very strict in their expectations of the code they're patching, and a call redirected by Farseek
    * will be unrecognized by Sponge and cause a crash if mandatory. Since Farseek is more lenient about patching modified code,
    * we move it after the Sponge proxy. (This cannot be done with @SortIndex since Sponge places itself last manually.)
    * We also need to exclude the Farseek transformer from Sponge's pre-mixin transformations performed by the proxy.
    * For these pre-mixin transformations, we use the FarseekSpongeClassTransformer which only transforms Sponge mixin classes.
    * Finally we re-enable transformations on TrackingUtil so we can intercept calls such as Block.updateTick(); this may break what
    * https://github.com/SpongePowered/SpongeForge/commit/7c5bb8ac8d7cd9e9098f06c6a2242103e1c0c614#diff-1e61645be86b333938a9f9575cf5fc81
    * was fixing, so we will look for an alternative if possible.
    */
  private def moveAfterSponge(): Unit = {
    if(!checkedSponge) {
      checkedSponge = true
      val transformers = new java.util.ArrayList(Launch.classLoader.getTransformers)
      if(transformers.exists(_.getClass.getName == "org.spongepowered.asm.mixin.transformer.Proxy")) {
        info("Moving Farseek transformer after Sponge proxy")
        val wrapper = transformers.find(_.toString.contains(classOf[FarseekClassTransformer].getName)).get
        transformers.remove(wrapper)
        transformers.add(wrapper)
        val transformersField = Launch.classLoader.getClass.getDeclaredField("transformers")
        transformersField.setAccessible(true)
        transformersField.set(Launch.classLoader, transformers)
        info("Excluding Farseek transformer from Sponge pre-mixin transformations")
        val mixinEnvironmentClass = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment")
        val mixinEnvironment = mixinEnvironmentClass.getDeclaredMethod("getCurrentEnvironment").invoke(null)
        mixinEnvironmentClass.getDeclaredMethod("addTransformerExclusion", classOf[String]).invoke(mixinEnvironment, classOf[FarseekClassTransformer].getName)
        info("Re-enabling transformations on Sponge TrackingUtil")
        // Undoes https://github.com/SpongePowered/SpongeForge/commit/7c5bb8ac8d7cd9e9098f06c6a2242103e1c0c614#diff-1e61645be86b333938a9f9575cf5fc81
        val transformerExclusionsField = Launch.classLoader.getClass.getDeclaredField("transformerExceptions")
        transformerExclusionsField.setAccessible(true)
        transformerExclusionsField.get(Launch.classLoader).asInstanceOf[java.util.Set[String]].remove("org.spongepowered.common.event.tracking.TrackingUtil")
      }
    }
  }
}

class FarseekSpongeClassTransformer extends IClassTransformer {
  def transform(obfuscatedName: String, deobfuscatedName: String, bytecode: Array[Byte]): Array[Byte] = {
    if(bytecode == null || !deobfuscatedName.startsWith("org.spongepowered.common.mixin")) bytecode
    else new FarseekClassVisitor(bytecode, internalName(deobfuscatedName), replacements).patch
  }
}

object MethodReplacements extends Logging {

  private val ReplacementsFilepath = "META-INF/farseek_cm.cfg"

  lazy val replacements: Map[ReplacedMethod, MethodReplacement] = {
    val modsDir = new File(gameDir, "mods")
    val allReplacements = ((files(modsDir) ++ files(new File(modsDir, mcVersion))).flatMap(methodReplacements) ++
      Launch.classLoader.getResources(ReplacementsFilepath).flatMap(methodReplacements)).toSet
    allReplacements.groupBy(_._1).values.find(_.size > 1).foreach(conflicts =>
      sys.error(s"Found conflicting method replacements:\n  ${conflicts.mkString("\n")}"))
    allReplacements.toMap
  }

  private def methodReplacements(file: File): Seq[(ReplacedMethod, MethodReplacement)] = {
    trace(s"Checking $file for method replacements")
    if(!file.getName.endsWith(".jar")) Seq()
    else {
      try logged(file, using(new ZipFile(file))(zipFile =>
        Option(zipFile.getEntry(ReplacementsFilepath)).fold(Seq[(ReplacedMethod, MethodReplacement)]())(entry =>
          methodReplacements(zipFile.getInputStream(entry)))))
      catch {
        case ze: ZipException =>
          warn(s"$file: ${ze.getLocalizedMessage}")
          Seq()
      }
    }
  }

  private def methodReplacements(url: URL): Seq[(ReplacedMethod, MethodReplacement)] = logged(url, methodReplacements(url.openStream))

  private def methodReplacements(stream: InputStream): Seq[(ReplacedMethod, MethodReplacement)] =
    using(fromInputStream(stream))(_.getLines().filter(_.takeWhile(_ != '#').trim.nonEmpty).map(methodReplacement).toList)

  private def methodReplacement(line: String): (ReplacedMethod, MethodReplacement) = {
    val Array(replacementClass, declaringClass, devName, srgName, descriptor) = line.split("\\s+")
    ReplacedMethod(internalName(declaringClass), if(isDev) devName else srgName, descriptor) ->
      MethodReplacement(internalName(replacementClass), devName)
  }

  private def logged(source: Any, replacements: Seq[(ReplacedMethod, MethodReplacement)]): Seq[(ReplacedMethod, MethodReplacement)] = {
    if(replacements.nonEmpty) debug(s"Found method replacements in $source:\n  ${replacements.mkString("\n  ")}")
    replacements
  }
}

case class ReplacedMethod(className: String, methodName: String, descriptor: String)

case class MethodReplacement(className: String, methodName: String)
