package filters

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Yerlibilgin
  *         This filter ensures that if the request contains a header called
  *         MRC_SEQUENCE_NUMBER it is passed back to the response.
  */
@Singleton
class SequenceNumberResponder @Inject()(implicit override val mat: Materializer,
                                        ec: ExecutionContext) extends Filter {


  override def apply(nextFilter: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val seqNum = rh.headers.get("MRC_SEQUENCE_NUMBER").getOrElse(null)
    if (seqNum == null) {
      nextFilter(rh)
    } else {
      nextFilter(rh).map { result =>
        Logger.debug(s"Respond with MRC_SEQUENCE_NUMBER=$seqNum")
        result.withHeaders("MRC_SEQUENCE_NUMBER" -> seqNum)
      }
    }
  }
}
