package farseek.core

import farseek.util.Logging
import org.objectweb.asm.ClassWriter._
import org.objectweb.asm._

/** Convenience base class for Farseek [[ClassVisitor]]s.
  * @author delvr
  */
abstract class ClassPatcher(reader: ClassReader) extends ClassVisitor(AsmVersion, new ClassWriter(reader, COMPUTE_MAXS)) with Logging {

    def this(bytecode: Array[Byte]) = this(new ClassReader(bytecode))

    def patch: Array[Byte] = {
        reader.accept(this, 0)
        cv.asInstanceOf[ClassWriter].toByteArray
    }
}
