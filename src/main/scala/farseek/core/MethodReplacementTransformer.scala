package farseek.core

/** A class transformer that applies [[FarseekClassVisitor]], followed by a [[MethodReplacer]] for each `methodReplacement`.
  * Excludes COFH world proxies (`skyboy/core/world/WorldProxy` and `skyboy/core/world/WorldServerProxy`) due to ASM conflicts.
  * @author delvr
  */
abstract class MethodReplacementTransformer extends FarseekBaseClassTransformer {

    protected def methodReplacements: Seq[MethodReplacement]

    // Use -Dlegacy.debugClassLoading=true for more information when testing this.
    protected def transform(name: String, bytecode: Array[Byte]) = {
        // Exclude COFH world proxies, since COFH would duplicate our replacement methods
        // (by failing to recognize and delete the originals) when applying the proxy.
        // Source: https://github.com/CoFH/CoFHCore/blob/master/src/main/java/cofh/asm/ASMCore.java
        if(name == "skyboy/core/world/WorldProxy" || name == "skyboy/core/world/WorldServerProxy")
            bytecode
        else {
            var result = new FarseekClassVisitor(bytecode, name, methodReplacements).patch
            for(replacement <- methodReplacements)
                result = new MethodReplacer(result, name, replacement).patch
            result
        }
    }
}

/** Specifies a method for replacement by a [[MethodReplacer]], to be matched by name and descriptor.
  * Requires the deobfuscated `dev` name the semi-obfuscated "SRG name" (as found in the mcp-srg.srg file).
  * @author delvr
  */
case class MethodReplacement(className: String, devName: String, srgName: String, descriptor: String,
                             replacementClass: String, replacementMethod: String) {

    def matches(methodName: String, methodDescriptor: String) =
        (devName == methodName || srgName == methodName) && descriptor == methodDescriptor
}

/** Factory for [[MethodReplacement]]s. Provides a shortcut for cases where the method has no SRG name.
  * @author delvr
  */
object MethodReplacement {

    val ReplacementPrefix = "REPLACED_"

    def apply(className: String, devName: String, srgName: String, descriptor: String, replacement: String): MethodReplacement = {
        val (replacementClass, replacementMethod) = classAndMethodNames(replacement)
        MethodReplacement(className, devName, srgName, descriptor, replacementClass, replacementMethod)
    }

    def apply(className: String, name: String, descriptor: String, replacement: String): MethodReplacement =
        apply(className, name, name, descriptor, replacement)

    @deprecated(message = "Use apply(String, String, String, String, String)", since = "1.0.7")
    def apply(className: String, devName: String, srgName: String, descriptor: String, replacement: String,
              transformer: MethodReplacementTransformer): MethodReplacement =
        apply(className, devName, srgName, descriptor, replacement)

    @deprecated(message = "Use apply(String, String, String, String)", since = "1.0.7")
    def apply(className: String, name: String, descriptor: String, replacement: String,
              transformer: MethodReplacementTransformer): MethodReplacement =
        apply(className, name, descriptor, replacement)
}
