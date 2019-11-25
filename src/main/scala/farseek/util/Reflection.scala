package farseek.util

import java.lang.reflect.Modifier._
import java.lang.reflect._
import scala.reflect._

/** Reflection utility functions.
  * @author delvr
  */
object Reflection {

    private val fieldModifiersField: Field = {
        val field = classOf[Field].getDeclaredField("modifiers")
        field.setAccessible(true)
        field
    }

    def classFields[T: ClassTag](classe: Class[_]): Seq[Field] =
        classe.getDeclaredFields.toSeq.filter(f => classTag[T].runtimeClass.isAssignableFrom(f.getType)).map(_.accessible)

    def classFields[T: ClassTag, C: ClassTag]: Seq[Field] = classFields[T](classTag[C].runtimeClass)

    def classFieldValues[T: ClassTag, C: ClassTag]: Seq[T] = classFields[T, C].map(_.value[T]())

    def classFieldValues[T: ClassTag](instance: Any): Seq[T] = classFields[T](instance.getClass).map(_.value[T](instance))

    /** Recursively copies the values of all instance fields of `from` to `to`, excluding fields matching `fieldExclusion`,
      * starting with common class/superclass `startClass` and moving up through superclasses. */
    def cloneObject[T: ClassTag](startClass: Class[_], from: T, to: T, fieldExclusion: Field => Boolean = _ => false) {
        startClass.getDeclaredFields.filterNot(field => isStatic(field.getModifiers) || fieldExclusion(field)).foreach(_.copyValue(from, to))
        Option(startClass.getSuperclass).foreach(cloneObject(_, from, to, fieldExclusion))
    }

    /** Value class for [[Field]]s with utility methods. */
    implicit class FieldValue(val field: Field) extends AnyVal {

        /** Makes `field` public and non-final. */
        def accessible: Field = if(field.isAccessible && !isFinal(field.getModifiers)) field else {
            fieldModifiersField.setInt(field, field.getModifiers & ~FINAL)
            field.setAccessible(true)
            field
        }

        def apply[T](instance: Any = null): T = value(instance)

        def value[T](instance: Any = null): T = accessible.get(instance).asInstanceOf[T]

        def setValue(value: Any, instance: Any = null) { accessible.set(instance, value) }

        def copyValue(from: Any, to: Any) { setValue(value(from), to) }
    }

    /** Value class for [[Method]]s with utility methods. */
    implicit class MethodValue(val method: Method) extends AnyVal {

        def accessible: Method = if(method.isAccessible) method else { method.setAccessible(true); method }

        def apply[T >: Null, R](obj: T, args: Any*): R =
            try method.invoke(obj, args.map(_.asInstanceOf[AnyRef]):_*).asInstanceOf[R]
            catch { case ex: InvocationTargetException => throw ex.getCause }
    }

    /** Value class for [[Constructor]]s with utility methods. */
    implicit class ConstructorValue[T](val ctor: Constructor[T]) extends AnyVal {

        def accessible: Constructor[T] = if(ctor.isAccessible) ctor else { ctor.setAccessible(true); ctor }

        def apply(args: Any*): T =
            try ctor.newInstance(args.map(_.asInstanceOf[AnyRef]):_*)
            catch { case ex: InvocationTargetException => throw ex.getCause }
    }

    implicit class ClassValue(val classe: Class[_]) extends AnyVal {

        def field(name: String): Option[Field] =
            try Some(classe.getDeclaredField(name).accessible)
            catch { case _: NoSuchFieldException => None }

        def constructor[T](params: Class[_]*): Option[Constructor[T]] =
            try Some(classe.getDeclaredConstructor(params:_*).accessible.asInstanceOf[Constructor[T]])
            catch { case _: NoSuchMethodException => None }

        def declaredMethod(name: String, params: Class[_]*): Option[Method] =
            try Some(classe.getDeclaredMethod(name, params:_*).accessible)
            catch { case _: NoSuchMethodException => None }

        def publicMethod(name: String, params: Class[_]*): Option[Method] =
          try Some(classe.getMethod(name, params:_*).accessible)
          catch { case _: NoSuchMethodException => None }
    }
}
