package farseek

import cpw.mods.fml.common.Mod

/** Non-core mod class for Farseek. `@Mod` annotation parameters for mod and dependencies versions should be replaced by the build process.
  * @see [[farseek.core.FarseekCoreMod for core mod class.
  * @author delvr
  */
@Mod(modLanguage = "scala", modid = "farseek", version = "SNAPSHOT", dependencies = "required-after:Forge;after:terrafirmacraft")
object FarseekMod extends FarseekBaseMod {

    val name = "Farseek"
    val description = "A Scala modding API based on Forge."
    val authors = Seq("delvr")

    val configuration = None
 }
