import com.google.inject.AbstractModule
import minderengine.{MinderServer, XoolaServer}
import utils.Startup

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[MinderServer]).asEagerSingleton()
    bind(classOf[XoolaServer]).asEagerSingleton()
    bind(classOf[Startup]).asEagerSingleton()
  }
}
