package farseek.client

import farseek.config.ConfigCategory

/** A [[ConfigScreen]] for a category of configuration options.
  * @author delvr
  */
class CategoryConfigScreen(parent: ConfigScreen, val category: ConfigCategory) extends ConfigScreen(parent)
