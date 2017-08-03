package utils

import java.io.{ByteArrayInputStream, FileInputStream}
import javax.xml.bind.{JAXBContext, JAXBElement}

import play.api.mvc._
import rest.models.runModel.ObjectFactory

import scala.concurrent.Future

class UserRequest[A](val username: String, val requestObject: Option[Object], request: Request[A]) extends WrappedRequest[A](request)

object UserAction extends
    ActionBuilder[UserRequest] with ActionTransformer[Request, UserRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    if (request.body.isInstanceOf[RawBuffer]) {
      request.body.asInstanceOf[RawBuffer].asBytes().map { str =>
        var jaxbContext = JAXBContext.newInstance(classOf[ObjectFactory]);
        val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
        val result = jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(str.toArray)).asInstanceOf[JAXBElement[Object]];
        new UserRequest(request.tags("email"), Option(result.getValue), request)
      }.getOrElse(null)
    } else {
      new UserRequest(request.tags("email"), None, request)
    }
  }
}