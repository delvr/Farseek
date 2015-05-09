package farseek.core

import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import net.minecraft.launchwrapper._

/** Convenience base class for Farseek core mods.
  * @author delvr
  */
abstract class FarseekBaseCoreMod extends IFMLLoadingPlugin {

    excludeFromTransformation(getClass.getPackage.getName)
    excludeFromTransformation("scala")

    protected def excludeFromTransformation(prefix: String) {
        getClass.getClassLoader.asInstanceOf[LaunchClassLoader].addTransformerExclusion(prefix)
    }

    protected def transformerClasses: Seq[Class[_ <: IClassTransformer]]

    def getASMTransformerClass = transformerClasses.map(_.getName).toArray

    def getModContainerClass = null

    def getAccessTransformerClass = null

    def getSetupClass = null

    def injectData(data: java.util.Map[String, AnyRef]) {}

    override def toString = getClass.getName
}
