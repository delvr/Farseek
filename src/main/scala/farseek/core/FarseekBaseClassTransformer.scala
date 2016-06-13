package farseek.core

import net.minecraft.launchwrapper._

/** Farseek implementation of [[IClassTransformer]]. Validates that no net.minecraft classes are loaded during transformation.
  * Excludes all classes prefixed by entries in `excludedClassPrefixes`.
  * @author delvr
  */
abstract class FarseekBaseClassTransformer extends IClassTransformer with CoreLogging {

    private var transforming: Option[String] = None

    def transform(obfuscatedName: String, deobfuscatedName: String, bytecode: Array[Byte]) = {
        if(bytecode == null) null else {
            val name = internalName(deobfuscatedName)
            if(excludedClassPrefixes.exists(name.startsWith)) {
                debug(s"$this skipping excluded class $deobfuscatedName")
                bytecode
            } else {
                transforming.foreach { current =>
                    require(!isMinecraftClass(name), s"Minecraft class $name loaded while transforming class $current by core mod $this")
                }
                transforming = Some(name)
                try transform(name, bytecode)
                finally { transforming = None } // Need to be set for when Forge recovers from non-fatal errors like loading a non-existent Optifine "Config" class
            }
        }
    }

    protected def excludedClassPrefixes: Set[String]

    // Use -Dlegacy.debugClassLoading=true for more information when testing this.
    protected def transform(internalName: String, bytecode: Array[Byte]): Array[Byte]

    protected def isMinecraftClass(className: String) = className.startsWith("net/minecraft/")
}
