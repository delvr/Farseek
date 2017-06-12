package farseek

import cpw.mods.fml.common.Mod

/** Non-core mod class for Farseek.
  * @see [[farseek.core.FarseekCoreMod]] for core mod class.
  * @author delvr
  */
@Mod(modid = "farseek", modLanguage = "scala", useMetadata = true)
object FarseekMod extends FarseekBaseMod {
  val configuration = None
}
