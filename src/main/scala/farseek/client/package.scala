package farseek

import net.minecraft.client.renderer.BlockModelShapes

package object client {

  def registerAllBlocks(shapes: BlockModelShapes): Unit =
    try shapes.registerAllBlocks() catch {
      case _: NoClassDefFoundError => // Workaround for mods patching a method within this call but not being loaded due to a previous loading error
    }
}
