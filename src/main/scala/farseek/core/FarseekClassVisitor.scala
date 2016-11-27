package farseek.core

import farseek.util.Logging
import org.objectweb.asm.ClassWriter._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type._
import org.objectweb.asm._

abstract class ClassPatcher(reader: ClassReader) extends ClassVisitor(ASM5, new ClassWriter(reader, COMPUTE_MAXS)) {
  def patch: Array[Byte] = {
    reader.accept(this, 0)
    cv.asInstanceOf[ClassWriter].toByteArray
  }
}

class FarseekClassVisitor(bytecode: Array[Byte], className: String, replacements: Map[ReplacedMethod, MethodReplacement])
  extends ClassPatcher(new ClassReader(bytecode)) with Logging {

  override def visitMethod(accessFlags: Int, methodName: String, descriptor: String, signature: String, exceptions: Array[String]) =
    new MethodCallRedirector(super.visitMethod(accessFlags, methodName, descriptor, signature, exceptions), methodName)

  class MethodCallRedirector(visitor: MethodVisitor, methodName: String) extends MethodVisitor(api, visitor) {
    //trace(s"Visiting method $className/$methodName")
    override def visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) = {
      //trace(s"  Visiting method call to $owner/$name$descriptor")
      if(opcode == INVOKESPECIAL && owner != className) // super() call
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
      else replacements.get(ReplacedMethod(owner, name, descriptor)) match {
        case Some(MethodReplacement(replacementClassName, replacementMethodName)) if !(methodName == replacementMethodName &&
          (className == replacementClassName || className == replacementClassName+'$')) => // prevent recursion
          //trace(s"Redirecting method call in $className/$methodName from ${r.className}/${r.methodName}" +
          //      s" to ${r.replacementClassName}/${r.replacementMethodName}")
          val newDescriptor = if(opcode == INVOKESTATIC) descriptor
                              else descriptor.head + getObjectType(owner).getDescriptor + descriptor.tail
          super.visitMethodInsn(INVOKESTATIC, replacementClassName, replacementMethodName, newDescriptor, false)
        case _ => super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
      }
    }
  }
}
