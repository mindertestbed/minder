package e2etest

import java.text.SimpleDateFormat
import java.util.Calendar

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.EventSource
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee._
import play.api.libs.json.{Json, JsValue}
import play.libs.F.Promise

import scala.concurrent.{ExecutionContext, Future}

/**
 * This class performs an end-to-end
 * test case running a predefined TDL where
 * an xml-value-initiator, an xml-generator, a content verifier
 * and a reporting tool are used.
 */
@RunWith(classOf[JUnitRunner])
class SampleTests extends Specification {
  sequential


  val (chatOut, chatChannel) = Concurrent.broadcast[String]

  val prodT = new Thread() {
    override def run(): Unit = {
      val sdf = new SimpleDateFormat("HH:mm:ss")
      while (true) {
        chatChannel.push(sdf.format(Calendar.getInstance().getTime))
        Thread.sleep(1000);
      }
    }
  }

  prodT.start()


  myFunc

  def myFunc: Unit = {
    val s2 = chatOut &> EventSource()
    val consumer: Iteratee[String, Unit] = Iteratee.fold[String, Unit](0) {
      (_, str) =>
        println(str)
    }

    s2.run(consumer)

    Thread.sleep(100000);
  }
}
