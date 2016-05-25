package farseek

import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper._

/** Utility methods and value classes related to [[Entity]] objects.
  * @author delvr
  */
package object entity {

    /** Value class for [[Entity]] objects with utility methods. */
    implicit class EntityValue(val e: Entity) extends AnyVal {
        def x = floor_double(e.posX)
        def y = floor_double(e.posY)
        def z = floor_double(e.posZ)
        def xyz = (x, y, z)
    }
}
