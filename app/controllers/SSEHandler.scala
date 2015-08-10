package controllers

import java.util
import java.util.{Observable, Observer}

import minderengine.MinderWrapperRegistry
import models.{WrapperVersion, Job, Wrapper}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object SSEHandler extends Controller {

  val (wsOut, wsChannel) = Concurrent.broadcast[JsValue];

  MinderWrapperRegistry.get().addObserver(new Observer {
    override def update(o: Observable, arg: scala.Any): Unit = {
      val wrapperVersionId = arg.asInstanceOf[Long];
      val wrapperVersion = WrapperVersion.findById(wrapperVersionId);
      updateWrapperStatus(wrapperVersion, MinderWrapperRegistry.get().isWrapperAvailable(wrapperVersion))
    }
  })

  def updateWrapperStatus(wrapperVersion: WrapperVersion, online: Boolean): Unit = {
    val json = JsObject(Seq(
      "id" -> JsString(wrapperVersion.id + ""),
      "online" -> JsBoolean(online)))
    wsChannel.push(json);
  }

  def filterWS(labelMap: util.HashMap[String, models.WrapperVersion]) = Enumeratee.filter[JsValue] {
    tpl: JsValue => {
      labelMap.containsKey((tpl \ "id").as[String])
    }
  }

  class JobStatusRunnable(val labelMap: util.HashMap[String, models.WrapperVersion]) extends Runnable {
    override def run(): Unit = {
      if (labelMap != null) {
        Thread.sleep(1000)
        labelMap.foreach(wrp => {
          val online = MinderWrapperRegistry.get().isWrapperAvailable(wrp._2);
          val json = JsObject(Seq(
            "id" -> JsString(wrp._1 + ""),
            "online" -> JsBoolean(online)))
          wsChannel.push(json);
        });
      }
    }
  }

  val threadPool = java.util.concurrent.Executors.newFixedThreadPool(20);

  def wrapperStatusFeed(id: Long) = Action {
    println("Wrapper Status Feed " + id)
    val labelMap = new util.HashMap[String, models.WrapperVersion]()
    val job = Job.findById(id);
    job.mappedWrappers.foreach(mw => {
      mw.wrapperVersion = WrapperVersion.findById(mw.wrapperVersion.id);
      labelMap.put(mw.wrapperVersion.id + "", mw.wrapperVersion);
    })
    threadPool.submit(new JobStatusRunnable(labelMap))
    Ok.chunked(wsOut &> filterWS(labelMap) &> EventSource()).as("text/event-stream")
  }
}
