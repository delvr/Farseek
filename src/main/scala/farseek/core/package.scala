package farseek

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.Type._

/** ASM utility functions for the Farseek core mod.
  * @author delvr
  */
package object core {

    /** Ordering of Farseek among other core mods. We use a high number so we replace methods _after_ other mods have patched them. */
    final val FarseekCoreModSortIndex = 1000000

    val AsmVersion = Opcodes.ASM5

    /** Class representation of Type `t` for ASM purposes. */
    def asmTypeClass(t: Type): Class[_] = t.getSort match {
        case Type.VOID    => null
        case Type.BOOLEAN => classOf[Boolean]
        case Type.CHAR    => classOf[Char]
        case Type.BYTE    => classOf[Byte]
        case Type.SHORT   => classOf[Short]
        case Type.INT     => classOf[Int]
        case Type.FLOAT   => classOf[Float]
        case Type.LONG    => classOf[Long]
        case Type.DOUBLE  => classOf[Double]
        case _  => Class.forName(externalName(t.getInternalName))
    }

    /** Opcode/String representation of Type `t` for visiting stack frames. */
    def asmFrameType(t: Type): AnyRef = t.getSort match {
        case Type.VOID    => Opcodes.NULL
        case Type.BOOLEAN => Opcodes.INTEGER
        case Type.CHAR    => Opcodes.INTEGER
        case Type.BYTE    => Opcodes.INTEGER
        case Type.SHORT   => Opcodes.INTEGER
        case Type.INT     => Opcodes.INTEGER
        case Type.FLOAT   => Opcodes.FLOAT
        case Type.LONG    => Opcodes.LONG
        case Type.DOUBLE  => Opcodes.DOUBLE
        case _  => t.getInternalName
    }

    def returnType(descriptor: String): Class[_] = asmTypeClass(getReturnType(descriptor))

    def returnFrameType(descriptor: String): AnyRef = asmFrameType(getReturnType(descriptor))

    def parameterTypes(descriptor: String): Seq[Class[_]] = getArgumentTypes(descriptor).map(asmTypeClass)

    def parameterFrameTypes(descriptor: String): Array[AnyRef] = getArgumentTypes(descriptor).map(asmFrameType)

    def methodDescriptor(returnType: Any, parameterTypes: Any*): String = s"(${parameterTypes.map(typeDescriptor).mkString})${typeDescriptor(returnType)}"
    
    def typeDescriptor(classe: Any): String = classe match {
        case null => "V"
        case c: Class[_] => getDescriptor(c)
        case "" => ""
        case _ => s"L${internalName(classe.toString)};"
    }

    def internalName(classe: Any): String = {
        classe match {
            case c: Class[_] => c.getName
            case _ => classe.toString
        }
    }.replace('.', '/')
    
    def externalName(internalName: String) = internalName.replace('/', '.')

    def classAndMethodNames(s: String): (String, String) = {
        val i = {
            val iDot = s.lastIndexOf('.')
            if(iDot >= 0) iDot else s.lastIndexOf('/')
        }
        (s.substring(0, i), s.substring(i + 1))
    }

    def nameAndDescriptor(s: String): (String, String) = {
        val i = s.indexOf('(')
        (s.substring(0, i), s.substring(i))
    }

    def hasFlag(flags: Int, flag: Int) = (flags & flag) != 0
}
