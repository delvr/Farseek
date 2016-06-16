package farseek.core

/** A class transformer that applies [[FarseekClassVisitor]], followed by a [[MethodReplacer]] for each `methodReplacement`.
  * @author delvr
  */
abstract class MethodReplacementTransformer extends FarseekBaseClassTransformer {

    private var validated = false

    protected val excludedClassPrefixes: Set[String] = Set(
      /* Exclude COFH world proxies, since COFH would duplicate our replacement methods
         (by failing to recognize and delete the originals) when applying the proxy.
         Source: https://github.com/CoFH/CoFHCore/blob/master/src/main/java/cofh/asm/ASMCore.java */
      "skyboy/core/world/WorldProxy", "skyboy/core/world/WorldServerProxy",

      /* Exclude class hierarchies containing method with a client-only parameters we cannot delete in FarseekClassVisitor
         because it's called from the server side and prevented from crashing only by using null arguments. */
      "logisticspipes/textures/Textures", "logisticspipes/proxy/interfaces/IProxy", "logisticspipes/proxy/side/ServerProxy",
      "com/lordmau5/ffs/util/state/PropertyModel", "forestry/",

      /* Exclude classes with @Optional interfaces that don't strip out implemented methods. */
      "mcjty/immcraft/blocks/"
    )

    protected def methodReplacements: Seq[MethodReplacement]

    protected def transform(name: String, bytecode: Array[Byte]) = {
        require(validated || !methodReplacements.exists(replacement => excludedClassPrefixes.exists(replacement.className.startsWith)),
            s"Attempted to patch excluded class $name")
        validated = true
        var result = new FarseekClassVisitor(bytecode, name, methodReplacements).patch
        for(replacement <- methodReplacements)
            result = new MethodReplacer(result, name, replacement).patch
        result
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
