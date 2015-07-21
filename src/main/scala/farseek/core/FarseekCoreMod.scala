package farseek.core

import cpw.mods.fml.relauncher.IFMLLoadingPlugin._

/** Core mod class for Farseek.
  * @see [[farseek.FarseekMod]] for non-core mod class.
  * @author delvr
  */
@SortingIndex(value = FarseekCoreModSortIndex)
class FarseekCoreMod extends FarseekBaseCoreMod {

    protected def transformerClasses = Seq(classOf[FarseekClassTransformer])
}

/** [[MethodReplacementTransformer]] for Farseek.
  * Handles mod warnings/errors on loading existing worlds, and chunk generation extensions.
  * @author delvr
  */
class FarseekClassTransformer extends MethodReplacementTransformer {

    protected val methodReplacements = Seq(
        MethodReplacement("cpw/mods/fml/common/FMLContainer", "readData",
            "(Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Ljava/util/Map;Lnet/minecraft/nbt/NBTTagCompound;)V",
            "farseek/world/storage/SaveHandlerExtensions/readData"),

        MethodReplacement("cpw/mods/fml/common/registry/GameRegistry", "generateWorld",
            "(IILnet/minecraft/world/World;Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/chunk/IChunkProvider;)V",
            "farseek/world/gen/ChunkGeneratorExtensions/generateWorld"),

        MethodReplacement("cpw/mods/fml/common/eventhandler/EventBus", "post",
            "(Lcpw/mods/fml/common/eventhandler/Event;)Z",
            "farseek/world/gen/StandardTerrainDataProvider/postEvent"),

        MethodReplacement("com/bioxx/tfc/WorldGen/TFCChunkProviderGenerate", "generateTerrainHigh",
            "(II[Lnet/minecraft/block/Block;)V",
            "farseek/world/gen/TfcTerrainDataProvider/generateHighTerrain")
    )
}
