package controllers

import java.text.SimpleDateFormat
import java.util.{Observable, Observer, Calendar}

import minderengine.MinderWrapperRegistry
import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._

object SSEHandler extends Controller{
  val fmt = new SimpleDateFormat("HH:mm:ss")
  val (chatOut, chatChannel) = Concurrent.broadcast[String]

  val (wsOut, wsChannel) = Concurrent.broadcast[String];

  MinderWrapperRegistry.get().addObserver(new Observer {
    override def update(o: Observable, arg: scala.Any): Unit = {
      val label = arg.toString
      updateWrapperStatus(label, MinderWrapperRegistry.get().isWrapperAvailable(label))
    }
  })

  /** Controller action serving activity based on room */
  def clockFeed() = Action {
    println("Starting chunking")

    val h = new Thread(){
      override def run(): Unit ={
        while(true){
          chatChannel.push(fmt.format(Calendar.getInstance().getTime))
          Thread.sleep(10000);
        }
      }
    };
    h.start();
    Ok.chunked(chatOut &> EventSource()).as("text/event-stream")
  }

  def updateWrapperStatus(label: String, online: Boolean): Unit ={
    wsChannel.push(label + ": " + online);
  }

  def filterWS(status: String) = Enumeratee.filter[String] {
    st: String => st.startsWith(status)
  }

  def wrapperStatusFeed(label: String) = Action {
    println("WS Feed")
    val res = Ok.chunked(wsOut &> filterWS(label) &> EventSource()).as("text/event-stream")
    //for initial state, check the wrapper status, and push once.
    wsChannel.push(label + ": " + MinderWrapperRegistry.get().isWrapperAvailable(label));
    res;
  }
}