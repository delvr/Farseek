package farseek.world

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.world._
import net.minecraftforge.common.MinecraftForge._
import net.minecraftforge.event.world.WorldEvent
import scala.collection.mutable

/**
 * @author delvr
 */
abstract class WorldData[T] {

    EVENT_BUS.register(this)

    private val worldData = mutable.Map[World, T]()

    protected def newData(world: World): T

    def apply(world: World) = worldData.getOrElseUpdate(world, newData(world))

    @SubscribeEvent def onWorldUnload(event: WorldEvent.Unload) {
        worldData -= event.world
    }
}
