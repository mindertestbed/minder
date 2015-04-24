package controllers

import java.text.SimpleDateFormat
import java.util.Calendar

import play.api.libs.EventSource
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.mvc._

object TimeController extends Controller{
  val fmt = new SimpleDateFormat("HH:mm:ss")
  /** Central hub for distributing chat messages */
  val (chatOut, chatChannel) = Concurrent.broadcast[String]

  /** Controller action serving activity based on room */
  def clock() = Action {
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
}