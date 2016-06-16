package farseek.core

import jdk.internal.org.objectweb.asm.ClassWriter._
import jdk.internal.org.objectweb.asm.{ClassReader, _}

/** Convenience base class for Farseek [[ClassVisitor]]s.
  * @author delvr
  */
abstract class ClassPatcher(reader: ClassReader) extends ClassVisitor(AsmVersion, new ClassWriter(reader, COMPUTE_MAXS)) with CoreLogging {

    def this(bytecode: Array[Byte]) = this(new ClassReader(bytecode))

    def patch: Array[Byte] = {
        reader.accept(this, 0)
        cv.asInstanceOf[ClassWriter].toByteArray
    }
}
