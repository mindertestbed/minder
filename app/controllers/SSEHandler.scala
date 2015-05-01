package controllers

import java.text.SimpleDateFormat
import java.util
import java.util.{Observable, Observer, Calendar}

import minderengine.MinderWrapperRegistry
import models.{Wrapper, MappedWrapper, Job}
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.JavaConversions._

object SSEHandler extends Controller{

  val fmt = new SimpleDateFormat("HH:mm:ss")


  val (chatOut, chatChannel) = Concurrent.broadcast[String]
  val (wsOut, wsChannel) = Concurrent.broadcast[JsValue];

  MinderWrapperRegistry.get().addObserver(new Observer {
    override def update(o: Observable, arg: scala.Any): Unit = {
      val label = arg.toString;
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
    val wr = Wrapper.findByName(label);
    val json = JsObject(Seq(
      "label" -> JsString(label),
      "id" -> JsString(wr.id + ""),
      "online" -> JsBoolean(online)))
    wsChannel.push(json);
  }

  def filterWS(labelSet: util.HashMap[String, models.Wrapper]) = Enumeratee.filter[JsValue] {
    tpl : JsValue => {
      println("Filter " + tpl + " >>> " + labelSet.containsKey((tpl \ "label").as[String]))
      labelSet.containsKey((tpl \ "label").as[String])
    }
  }

  def wrapperStatusFeed(id: Long) = Action {
    println("WS Feed")
    val labelSet = new util.HashMap[String, models.Wrapper]()


    val job = Job.findById(id);
    job.mappedWrappers.foreach(mw =>
    {
      val wrp = Wrapper.findById(mw.wrapper.id);
      println("Mapped Wrapper " + wrp.name + " " + mw.wrapper.id )
      labelSet.put(wrp.name, wrp)}
    )
    new Thread (){override def run():Unit = {
      Thread.sleep(1000)
      labelSet.foreach(wrp => {
        println("Wrapper " + wrp._1)
        println("Wrapper " + wrp._2.name)
        val online = MinderWrapperRegistry.get().isWrapperAvailable(wrp._1);
        val json = JsObject(Seq(
          "label" -> JsString(wrp._2.name),
          "id" -> JsString(wrp._2.id + ""),
          "online" -> JsBoolean(online)))
        wsChannel.push(json);
      })
    }}.start()
    Ok.chunked(wsOut &> filterWS(labelSet) &> EventSource()).as("text/event-stream")
  }
}