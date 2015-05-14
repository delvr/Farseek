package farseek.core

import cpw.mods.fml.relauncher.FMLLaunchHandler._
import cpw.mods.fml.relauncher.Side._
import farseek.util.Logging
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._

/** A [[ClassPatcher]] that applies a [[SuperCallFixingMethodVisitor]] to each method.
  * On the server side, also removes any method with a parameter or return value from
  * [[cpw.mods.fml.relauncher.SideOnly]]-annotated classes in the net.minecraft.client.* packages.
  * This is meant for method overrides not annotated with [[cpw.mods.fml.relauncher.SideOnly]] that will be missed by
  * Forge's [[cpw.mods.fml.common.asm.transformers.SideTransformer]] and can cause classloading errors when using reflection.
  * @author delvr
  */
class FarseekClassVisitor(bytecode: Array[Byte], className: String, replacements: Seq[MethodReplacement]) extends ClassPatcher(bytecode) {

    import farseek.core.FarseekClassVisitor._

    override def visitMethod(accessFlags: Int, name: String, descriptor: String, signature: String, exceptions: Array[String]) = {
        if(side == SERVER && descriptor.contains(MinecraftClientPackage) && !UnannotatedClientClasses.exists(descriptor.contains)) {
            trace(s"Removing method with client-only net.minecraft.client parameters or return value: $className.$name")
            null
        } else {
            val visitor = super.visitMethod(accessFlags, name, descriptor, signature, exceptions)
            new SuperCallFixingMethodVisitor(className, visitor, replacements)
        }
    }
}

/** Companion object for [[FarseekClassVisitor]]s.
  * @author delvr
  */
object FarseekClassVisitor {

    private val MinecraftClientPackage = "net/minecraft/client/"

    /** Set of classes in [[MinecraftClientPackage]] that lack a [[cpw.mods.fml.relauncher.SideOnly]] annotation. */
    private val UnannotatedClientClasses = Set("model/ModelBase", "model/ModelBox", "model/ModelRenderer",
        "model/PositionTextureVertex", "model/TexturedQuad").map(MinecraftClientPackage + _)
}

/** A [[MethodVisitor]] that replaces super() calls to methods in `replacements` with calls to aliased methods created by [[MethodReplacer]]s
  * that contain the original implementations. This avoids duplicating functionality from executing replacements several times.
  * @author delvr
  */
class SuperCallFixingMethodVisitor(className: String, visitor: MethodVisitor, replacements: Seq[MethodReplacement])
        extends MethodVisitor(AsmVersion, visitor) with Logging {

    import farseek.core.MethodReplacement._

    // super.method() calls to replaced methods must target the original to avoid recursion
    override def visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
        val callTarget = {
            if(opcode == INVOKESPECIAL && owner != className) { // Same owner would mean a call to a private method
                replacements.find(_.matches(name, descriptor)) match {
                    case Some(replacement) =>
                        val newTarget = ReplacementPrefix + replacement.devName
                        trace(s"Redirecting a super() call in $className from patched $name to original $newTarget")
                        newTarget
                    case None => name
                }
            } else name
        }
        super.visitMethodInsn(opcode, owner, callTarget, descriptor, isInterface)
    }
}
