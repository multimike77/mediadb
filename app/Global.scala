import com.google.inject.AbstractModule
import com.google.inject.Guice
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter(shouldGzip =
  (request, response) => {
    val contentType = response.headers.get("Content-Type")
    contentType.exists(_.startsWith("text/html")) || request.path.endsWith("jsroutes.js")
  }
)) with GlobalSettings {
  /**
   * Create injector
   */
  val injector = Guice.createInjector(new AbstractModule {
    protected def configure() {
    }
  })

  /**
   * Controllers must be resolved through the application context. There is a special method of GlobalSettings
   * that we can override to resolve a given controller. This resolution is required by the Play router.
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = injector.getInstance(controllerClass)
}
