package farseek.core

import java.io.File
import java.util.zip.ZipFile
import org.apache.commons.io.IOUtils._
import scala.collection.JavaConversions._

/** A transformer that replaces net.minecraft classes with new versions packaged in the mod jar.
  * For inter-mod compatibility reasons, it is recommended to use [[MethodReplacementTransformer]]s instead.
  * @author delvr
  */
class ClassReplacementTransformer extends FarseekBaseClassTransformer {

    private val modJar: Option[ZipFile] = {
        val classLocation = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
        if(classLocation.isFile) Some(new ZipFile(classLocation))
        else None // running from IDE's build directory; classes are replaced by the IDE's load precedence instead
    }

    protected def transform(name: String, bytecode: Array[Byte]) = modJar match {
        case Some(jar) if isMinecraftClass(name) => jar.entries.find(_.getName == s"$name.class") match {
            case Some(entry) =>
                debug(s"Replacing class $name")
                toByteArray(jar.getInputStream(entry))
            case None => bytecode
        }
        case _ => bytecode
    }
}
