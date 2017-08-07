package rest

import java.util.Random
import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.Singleton
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * @author Bulut
  */
@Singleton
class RestKeyRenewer @Inject()(implicit configuration: Configuration,
                               actorSystem: ActorSystem, executionContext: ExecutionContext) {

  /**
    * Tüm oturumlar için en fazla aktif kalınabilecek süre. Bu süre her geçtiğinde periodKey yenilenir ve önceki periyodda kullanılan
    * tüm nonce değerleri geçersiz olur
    */
  val SESSION_PURGE_TIMEOUT = configuration.getString("SESSION_PURGE_TIMEOUT").getOrElse("5").toInt % 20


  /**
    * 32 BYTE uzunlugunda period anahtari. SESSION_PURGE_TIMEOUT sabitinde belirtilen
    * zaman dilimlerinde değiştirilir. Bu süre içerisinde kullanılan tüm oturumlar geçersiz hale gelir.
    * Tüm istemcilerin tekrar Digest authentication yapması gerekir. Böylece oturumların tümüne aynı anda bir ömür biçip
    * güvenlik için sınırlı tutmuş oluyoruz.
    */
  var periodKey: String = ""
  val tmpBuffer = new Array[Byte](32)

  actorSystem.scheduler.schedule(0.second, SESSION_PURGE_TIMEOUT.minute) {
    Logger.trace("Rest session key being renewed")
    new Random().nextBytes(tmpBuffer);
    periodKey = new String(tmpBuffer)
  }


  /**
    * 16 byte uzunlugunda bir byte dizisi uretir.
    *
    * See: OPAQUE in RFC7616
    * https://tools.ietf.org/html/rfc7616#page-7
    *
    * @return
    */
  def generateOpaque = {
    val bytes = new Array[Byte](16)
    new Random().nextBytes(bytes);
    bytes
  }
}
