import java.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import play.api.libs.EventSource
import play.api.libs.iteratee.Input.{EOF, El}
import play.api.libs.iteratee.{Concurrent, Input, Iteratee}
import play.api.libs.streams.Streams
import views.html.helper.FieldConstructor

/**
  * Created by yerlibilgin on 04/05/15.
  */
object MyHelper {
  implicit val myFields = FieldConstructor(views.html.util.myInput.f)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def main(args: Array[String]): Unit = {

    import scala.concurrent.ExecutionContext.Implicits.global

    val (chatEnumerator, chatChannel) = Concurrent.broadcast[String]
    val chatClient1 = Iteratee.foreach[String](m => {
      println(Thread.currentThread().getName + " Client 1: " + m)
    })
    val chatClient2 = Iteratee.foreach[String](m => {
      println(Thread.currentThread().getName + " Client 2: " + m)
    })
    chatEnumerator |>>> chatClient1
    chatEnumerator |>>> chatClient2

    new Thread(){
      override def run(){
        val source = Source.fromPublisher(Streams.enumeratorToPublisher(chatEnumerator))
        val result = source// via EventSource.flow
        println("here")
        //result.
        println("and now here")
      }
    }.start();

    chatChannel.push(El("Hello world 1"))
    chatChannel.push(El("Hello world 2"))
    chatChannel.push(El("Hello world 3"))
    chatChannel.push(El("Hello world 4"))
    chatChannel.push(EOF)

    println("MAIN: " + Thread.currentThread().getName)

    Thread.sleep(5000)

  }
}
