package filters

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import play.api.mvc._
import play.api.{Configuration, Logger}
import rest.{Failure, RestAuthUtil, StaleNonce, Success}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Rest Login USIN RFC 2617
  *
  * @author Yerlibilgin
  *
  */
@Singleton
class RESTAuthFilter @Inject()(implicit override val mat: Materializer, ec: ExecutionContext,
                               configuration: Configuration,
                               restAuthUtil: RestAuthUtil) extends Filter {
  override def apply(nextFilter: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.path.startsWith("/rest")) {
      rh.headers.get(RestAuthUtil.AUTHORIZATION).map { header =>
        try {
          restAuthUtil.checkAuthorizationHeader(rh.method, header) match {
            case StaleNonce() => {
              Logger.debug("Stale Nonce detected")
              sendBackNonce(stale = true)
            }
            case Failure() => {
              Logger.debug("REST Login failed.")
              sendBackNonce()
            }
            case Success(userName) => {
              Logger.debug("REST Login succsessful apply next filter")
              Logger.debug(rh.path)
              nextFilter(rh.withTag("email", userName))
            }
          }
        } catch {
          case ex: IllegalStateException => {
            Logger.error("Too many requests")
            Future.successful(Results.TooManyRequests)
          }
        }
      }.getOrElse {
        Logger.debug("Rest request without an authorization header. Send back WWW-Authenticate header")
        sendBackNonce()
      }
    } else {
      nextFilter(rh);
    }
  }

  def sendBackNonce(stale: Boolean = false): Future[Result] = {
    Logger.debug("Send back a new nonce")
    val nonce = restAuthUtil.createNonce()
    Future.successful {
      val header: (String, String) = restAuthUtil.createDigestHeader(nonce, stale)
      Results.Unauthorized("UNAUTHORIZED").
          withHeaders(header).withNewSession
    }
  }
}