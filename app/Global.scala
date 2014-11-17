import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import minderengine.TimeProvider


object Global extends GlobalSettings {
  val startTime = TimeProvider.getDate()

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  /**
   * If a request does not find a target, show a default page
   */
  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      views.html.notfound(request.path)))
  }

  /**
   * On bad request
   */
  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request: " + error))
  }
  
  //TODO: initialize Xoola server for remotely wrapped objects
}
