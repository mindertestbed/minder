package utils

import play.api.mvc._
import play.core.j.JavaHelpers
import play.mvc.Http

import scala.concurrent.Future

    object JavaAction extends
        ActionBuilder[Request] with ActionFunction[Request, Request] {
      def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
        withContext(request) {
          block(request);
        }
      }

      def withContext[Status, A](request: Request[A])(block: => Status): Status = {
        try{
          Http.Context.current.set(JavaHelpers.createJavaContext(request))
          block
        } finally {
          Http.Context.current.remove()
        }
      }
    }
