package farseek.core

import farseek.util.Logging
import net.minecraftforge.fml.common.FMLLog

trait CoreLogging extends Logging {
  override protected val logger = FMLLog.getLogger
}
