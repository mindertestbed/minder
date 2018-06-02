package controllers

import java.util
import java.util.{Observable, Observer}
import javax.inject.Inject

import akka.stream.scaladsl.Source
import minderengine.MinderAdapterRegistry
import models.{AdapterVersion, Job}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.json._
import play.api.libs.streams.Streams
import play.api.mvc._

import scala.collection.JavaConversions._

class AdapterStatusFeeder @Inject()() extends Controller {

  val (wsOut, wsChannel) = Concurrent.broadcast[JsValue];
  val (adapterStatusOut, adapterStatusChannel) = Concurrent.broadcast[(AdapterVersion, Boolean)];

  MinderAdapterRegistry.get().addObserver(new Observer {
    override def update(o: Observable, arg: scala.Any): Unit = {
      val adapterVersionId = arg.asInstanceOf[Long];
      val adapterVersion = AdapterVersion.findById(adapterVersionId);
      updateAdapterStatusOld(adapterVersion, MinderAdapterRegistry.get().isAdapterAvailable(adapterVersion))

      updateAdapterStatus(adapterVersion, MinderAdapterRegistry.get().isAdapterAvailable(adapterVersion))
    }
  })

  /**
    * Obsolete, remove it in the future
    *
    * @param adapterVersion
    * @param online
    */
  def updateAdapterStatusOld(adapterVersion: AdapterVersion, online: Boolean): Unit = {
    val json = JsObject(Seq(
      "id" -> JsString(adapterVersion.id + ""),
      "online" -> JsBoolean(online)))
    wsChannel.push(json);

  }

  def updateAdapterStatus(adapterVersion: AdapterVersion, online: Boolean): Unit = {
    adapterStatusChannel.push((adapterVersion, online))
  }

  def filterWS(labelMap: util.HashMap[String, AdapterVersion]) = Enumeratee.filter[JsValue] {
    tpl: JsValue => {
      labelMap.containsKey((tpl \ "id").as[String])
    }
  }


  class AdapterStatusRunnable(val labelMap: util.HashMap[String, AdapterVersion]) extends Runnable {
    override def run(): Unit = {
      if (labelMap != null) {
        Thread.sleep(2000)
        labelMap.foreach(wrp => {
          val online = MinderAdapterRegistry.get().isAdapterAvailable(wrp._2);
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
    val labelMap = new util.HashMap[String, AdapterVersion]()
    val job = Job.findById(jobId);
    job.mappedAdapters.foreach(mw => {
      mw.adapterVersion = AdapterVersion.findById(mw.adapterVersion.id);
      labelMap.put(mw.adapterVersion.id + "", mw.adapterVersion);
    })
    threadPool.submit(new AdapterStatusRunnable(labelMap))
    val source = Source.fromPublisher(Streams.enumeratorToPublisher(wsOut &> filterWS(labelMap)));
    Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }


  def filterAdapterId(id: Long) = Enumeratee.filter[(AdapterVersion, Boolean)] {
    tpl => true
  }

  def renderAdapterStatus(): Enumeratee[(AdapterVersion, Boolean), JsValue] = Enumeratee.map[(AdapterVersion, Boolean)] {
    tpl => {
      JsObject(Seq(
        "versionId" -> JsString(tpl._1.id + ""),
        "adapterId" -> JsString(tpl._1.adapter.id + ""),
        "name" -> JsString(tpl._1.adapter.name + ""),
        "online" -> JsBoolean(tpl._2)))
    }
  }

  def adapterStatusFeed(id: Long) = Action {
    new Thread() {
      override def run(): Unit = {
        Thread.sleep(1000)
        for (wr <- models.Adapter.getAll()) {
          val wv: AdapterVersion = AdapterVersion.latestByAdapter(wr)
          if (wv != null)
            adapterStatusChannel.push((wv, MinderAdapterRegistry.get().isAdapterAvailable(wv)))
        }
      }
    }

    val source = Source.fromPublisher(Streams.enumeratorToPublisher(adapterStatusOut &> filterAdapterId(id) &> renderAdapterStatus()));
    Ok.chunked(source via EventSource.flow).as("text/event-stream")
  }
}
