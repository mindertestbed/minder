package e2etest

import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.xml.bind.DatatypeConverter

import controllers.Application
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._
import play.libs.Json
import play.test

/**
 * This class performs an end-to-end
 * test case running a predefined TDL where
 * an xml-value-initiator, an xml-generator, a content verifier
 * and a reporting tool are used.
 */
@RunWith(classOf[JUnitRunner])
class SampleTestAssertionSpec extends Specification {

  val tdl = """def bookXsd = scala.io.Source.fromFile("books.xsd").mkString.getBytes

  TestCase = "XMlGeneratorTest"

  val rivet1 = "generateBooksData(int)" of "xmlValueInitiator" shall(
     map(5 onto 1)
  )

  val rivet2 = "generateXML(byte[])" of "xmlGenerator" shall(
     use("initialDataCreated(byte[])" of "xmlValueInitiator")(
       mapping(1 onto 1)
     )
  )

  /////////////////////////////
  /////////////////////////////
  val rivet3 = "verifyXsd(byte[],byte[])" of "xml-content-verifier" shall(
     use("xmlProduced(byte[])" of "xmlGenerator")(
       mapping(1 onto 2)
     ),
     map(bookXsd onto 1))"""

  sequential

  "Application" should {
    "run the sample test from testing page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)

      Thread.sleep(1000)

      /*now run the clients*/

      //run xml-value-initiator
      val properties = new java.util.Properties();
      var props =
      """WRAPPER_NAME=xmlValueInitiator
HOST=localhost
PORT=25000
SERVERID=minderServer
PING_TIMEOUT=20
NETWORK_BUFFER_SIZE = 20480
NETWORK_RESPONSE_TIMEOUT = 50000
WRAPPER_CLASS=wrapper.XmlValueInitiatorWrapper"""
      properties.load(new StringReader(props))
      var valueInitiatorClient = new MinderClient(properties)

      //run xml-generator
      props =
      """WRAPPER_NAME=xmlGenerator
HOST=localhost
PORT=25000
SERVERID=minderServer
PING_TIMEOUT=20
NETWORK_BUFFER_SIZE = 20480
NETWORK_RESPONSE_TIMEOUT = 50000
WRAPPER_CLASS=wrapper.XmlGeneratorWrapper"""
      properties.load(new StringReader(props))
      var xmlGeneratorClient = new MinderClient(properties)
      //run reporter
      Thread.sleep(500)

      //run test
      val op = route(FakeRequest(POST, "/testme").withTextBody(tdl).withSession(("userEmail", "massimiliano.masi@tiani-spirit.com")))
      val testPage = op.get
      status(testPage)(akka.util.Timeout(50, TimeUnit.SECONDS)) must equalTo(OK)
    }
  }
}
