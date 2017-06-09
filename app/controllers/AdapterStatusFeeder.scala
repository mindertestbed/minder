package controllers

import java.util
import java.util.{Observable, Observer}
import javax.inject.Inject

import akka.actor.ActorSystem
import minderengine.MinderWrapperRegistry
import models.{Job, TestRun, WrapperVersion}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

class AdapterStatusFeeder @Inject()() extends Controller {

  val (wsOut, wsChannel) = Concurrent.broadcast[JsValue];
  val (adapterStatusOut, adapterStatusChannel) = Concurrent.broadcast[(WrapperVersion, Boolean)];

  MinderWrapperRegistry.get().addObserver(new Observer {
    override def update(o: Observable, arg: scala.Any): Unit = {
      val wrapperVersionId = arg.asInstanceOf[Long];
      val wrapperVersion = WrapperVersion.findById(wrapperVersionId);
      updateAdapterStatusOld(wrapperVersion, MinderWrapperRegistry.get().isWrapperAvailable(wrapperVersion))

      updateAdapterStatus(wrapperVersion, MinderWrapperRegistry.get().isWrapperAvailable(wrapperVersion))
    }
  })

  /**
    * Obsolete, remove it in the future
    *
    * @param wrapperVersion
    * @param online
    */
  def updateAdapterStatusOld(wrapperVersion: WrapperVersion, online: Boolean): Unit = {
    val json = JsObject(Seq(
      "id" -> JsString(wrapperVersion.id + ""),
      "online" -> JsBoolean(online)))
    wsChannel.push(json);

  }

  def updateAdapterStatus(wrapperVersion: WrapperVersion, online: Boolean): Unit = {
    adapterStatusChannel.push((wrapperVersion, online))
  }

  def filterWS(labelMap: util.HashMap[String, models.WrapperVersion]) = Enumeratee.filter[JsValue] {
    tpl: JsValue => {
      labelMap.containsKey((tpl \ "id").as[String])
    }
  }


  class AdapterStatusRunnable(val labelMap: util.HashMap[String, models.WrapperVersion]) extends Runnable {
    override def run(): Unit = {
      if (labelMap != null) {
        Thread.sleep(2000)
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

  /**
    * Filter adapter states with respoect to the job id
    *
    * @param jobId
    * @return
    */
  def adapterJobStatusFeed(jobId: Long) = Action {
    val labelMap = new util.HashMap[String, models.WrapperVersion]()
    val job = Job.findById(jobId);
    job.mappedWrappers.foreach(mw => {
      mw.wrapperVersion = WrapperVersion.findById(mw.wrapperVersion.id);
      labelMap.put(mw.wrapperVersion.id + "", mw.wrapperVersion);
    })
    threadPool.submit(new AdapterStatusRunnable(labelMap))
    Ok.chunked(wsOut &> filterWS(labelMap) &> EventSource()).as("text/event-stream")
  }


  def filterAdapterId(id: Long) = Enumeratee.filter[(WrapperVersion, Boolean)] {
    tpl => true
  }

  def renderAdapterStatus(): Enumeratee[(WrapperVersion, Boolean), JsValue] = Enumeratee.map[(WrapperVersion, Boolean)] {
    tpl => {
      JsObject(Seq(
        "versionId" -> JsString(tpl._1.id + ""),
        "wrapperId" -> JsString(tpl._1.wrapper.id + ""),
        "name" -> JsString(tpl._1.wrapper.name + ""),
        "online" -> JsBoolean(tpl._2)))
    }
  }

  def adapterStatusFeed(id: Long) = Action {
    new Thread() {
      override def run(): Unit = {
        Thread.sleep(1000)
        for (wr <- models.Wrapper.getAll()) {
          val wv: WrapperVersion = WrapperVersion.latestByWrapper(wr)
          if (wv != null)
            adapterStatusChannel.push((wv, MinderWrapperRegistry.get().isWrapperAvailable(wv)))
        }
      }
    }
    Ok.chunked(adapterStatusOut &> filterAdapterId(id) &> renderAdapterStatus() &> EventSource()).as("text/event-stream")
  }
}
