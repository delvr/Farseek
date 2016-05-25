package farseek.core

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex

/** Core mod class for Farseek.
  * @see [[farseek.FarseekMod]] for non-core mod class.
  * @author delvr
  */
@SortingIndex(value = FarseekCoreModSortIndex)
class FarseekCoreMod extends FarseekBaseCoreMod {

    protected def transformerClasses = Seq(classOf[FarseekClassTransformer])
}

/** [[MethodReplacementTransformer]] for Farseek. Handles mod warnings/errors on loading existing worlds, and chunk generation extensions.
  * @author delvr
  */
class FarseekClassTransformer extends MethodReplacementTransformer {

    protected val methodReplacements = Seq(
        MethodReplacement("net/minecraftforge/fml/common/FMLContainer", "readData", "(Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Ljava/util/Map;Lnet/minecraft/nbt/NBTTagCompound;)V",
            "farseek/world/storage/SaveHandlerExtensions/readData"),
        MethodReplacement("net/minecraftforge/fml/common/registry/GameRegistry", "generateWorld", "(IILnet/minecraft/world/World;Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/chunk/IChunkProvider;)V",
            "farseek/world/gen/ChunkGeneratorExtensions/generateWorld")
    )
}
