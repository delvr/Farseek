package farseek.core

import farseek.util.Logging
import net.minecraft.launchwrapper._

/** Farseek implementation of [[IClassTransformer]]. Validates that no net.minecraft classes are loaded during transformation.
  * @author delvr
  */
abstract class FarseekBaseClassTransformer extends IClassTransformer with Logging {

    private var transforming: Option[String] = None

    def transform(obfuscatedName: String, deobfuscatedName: String, bytecode: Array[Byte]) = {
        if(bytecode == null) null else {
            val name = internalName(deobfuscatedName)
            transforming.foreach { current =>
                require(!isMinecraftClass(name), s"Minecraft class $name loaded while transforming class $current by core mod $this")
            }
            transforming = Some(name)
            try transform(name, bytecode)
            finally { transforming = None } // Need to be set for when Forge recovers from non-fatal errors like loading a non-existent Optifine "Config" class
        }
    }

    protected def transform(internalName: String, bytecode: Array[Byte]): Array[Byte]

    protected def isMinecraftClass(className: String) = className.startsWith("net/minecraft/")
}
