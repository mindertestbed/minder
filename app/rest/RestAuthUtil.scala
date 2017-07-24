package rest

import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import rest.MinderRestUtil._
import rest.controllers.common.Constants

/**
  * RFC 7616 Http Digest access authentication yardımcı sınıfı
  *
  *
  * <b>QOP (Quality of Protection):</b> auth olarak desteklenmektedir. auth-int desteği sunulmamaktadır. auth-int içeren istekler reddedilecektir.
  * <br/><b>&lt;HASH&gt;, &lt;HASH&gt;-sess</b> desteklenmektedir. HASH: MD5, SHA-256 desteklenmektedir. Onerilen SHA-256'dir
  *
  * @see https://tools.ietf.org/html/rfc7616
  * @author yerlibilgin
  *
  */
@Singleton
class RestAuthUtil @Inject()(configuration: Configuration, restKeyRenewer: RestKeyRenewer) {

  //static import the static part
  import RestAuthUtil._

  REALM = configuration.getString("REST_AUTH_REALM").getOrElse(Constants.MINDER_REALM)
  REST_AUTH_DIGEST_ALGORITHM = configuration.getString("REST_AUTH_DIGEST_ALGORITHM").getOrElse("MD5")
  MinderRestUtil.setSigestMethod(REST_AUTH_DIGEST_ALGORITHM)

  /**
    * Authorization header'i oluşturur
    *
    * @return
    */
  def createNonce(): Nonce = {
    val nonce = generateNonce
    nonce
  }

  /**
    * HTTP doğrulama gereksiniminin istemciye döndürüleceği zaman WWW-Authenticate header'ını hazırlar
    * ve döndürür
    *
    * @return
    */
  def createDigestHeader(nonce: Nonce, stale: Boolean = false): (String, String) = {
    WWW_AUTHENTICATE ->
        s"""Digest realm="${REALM}",
           |   nonce="${nonce.nonce}",
           |   qop="auth",
           |   algorithm=$REST_AUTH_DIGEST_ALGORITHM,
           |   stale=$stale,
           |   opaque="${nonce.opaque}"""".stripMargin
  }


  val separator = ":".getBytes()

  private def generateNonce = {
    val opaque = a2h(restKeyRenewer.generateOpaque)
    val timeStamp = System.currentTimeMillis()
    val hashInput = s"$timeStamp:$opaque:${restKeyRenewer.periodKey}"
    val nonce: String = s"$timeStamp${digest(hashInput)}"
    Logger.debug(s"Nonce generated timestamp: $timeStamp, Nonce: $nonce")
    Nonce(nonce, opaque)
  }

  /**
    * "Authorization" HEADER'i ile gelen nonce degerini dogrular
    *
    * https://tools.ietf.org/html/rfc2617  classical MD5
    * https://tools.ietf.org/html/rfc7616  more advanced hashing
    *
    * @param header
    * @return
    */
  def checkAuthorizationHeader(method: String, header: String): AuthorizationResult = {
    Logger.debug(s"Check authorization header $header");
    //check realm

    //remove the Digest part from the header
    val headerWithoutDigest = header.substring(header.indexOf("Digest") + 6).trim.replaceAll("\\s", "").replaceAll("\"", "").split("\\,")

    val headersAsMap = headerWithoutDigest.map(hv => {
      val eqIndex = hv.indexOf('=')

      hv.substring(0, eqIndex) -> hv.substring(eqIndex + 1)
    }).toMap

    Logger.trace(s"Authorization Header Parsed:$headersAsMap")

    val username = headersAsMap("username")
    val realm = headersAsMap("realm")
    val nonce = headersAsMap("nonce")
    val clientResponse = headersAsMap("response")
    val opaque = headersAsMap("opaque")
    val qop = headersAsMap("qop")
    val nc = headersAsMap("nc")
    val cnonce = headersAsMap("cnonce")

    //https://tools.ietf.org/html/rfc7616
    val uri = headersAsMap("uri")
    val algorithm = headersAsMap.getOrElse("algorithm", "MD5")

    //check the mandatory fields
    if (username.isEmpty || realm.isEmpty || nonce.isEmpty || clientResponse.isEmpty || opaque.isEmpty || qop.isEmpty || uri.isEmpty) {
      Logger.warn("One of the mandatory fields is empty")
      return Failure()
    }

    //additionally, since we specify qop, we need to check that cnonce and nc are available
    if (cnonce.isEmpty || nc.isEmpty) {
      Logger.warn("Nonce or client nonce empty")
      return Failure()
    }

    //support only qop=auth, reject auth-int
    if (qop.contains("auth-int")) {
      Logger.error(s"Unsopported Quality of protection $qop")
      return Failure()
    }

    if (REALM != realm) {
      Logger.error("Invalid realm value " + realm)
      return Failure()
    }


    //check whether I sent the nonce and if it is expired
    //val hashInput =

    val timeStamp = nonce.substring(0, 13)

    val hashInput = s"$timeStamp:$opaque:${restKeyRenewer.periodKey}"

    if (nonce != s"$timeStamp${digest(hashInput)}") {
      Logger.error("Invalid or expired nonce")
      return StaleNonce()
    }


    Logger.trace(s"Method: $method")
    //calculate A1
    try {
      val A1 = calculateA1(username, nonce, cnonce)

      Logger.debug(s"A1 $A1")

      /*
      RFC7616 says this:
          response = <"> < KD ( H(A1), unq(nonce)
            ":" nc
            ":" unq(cnonce)
            ":" unq(qop)
            ":" H(A2)
            ) <">
      */
      val A2 = digest(s"$method:$uri")
      Logger.debug(s"A2 $A2")

      Logger.debug(s"$A1:$nonce:$nc:$cnonce:$qop:$A2")

      val response = digest(s"$A1:$nonce:$nc:$cnonce:$qop:$A2")

      Logger.debug(s"Calculated Response: $response")

      //check the response
      if (clientResponse != response) {
        Logger.error(s"login for $username rejected, invalid response expected $response, received $clientResponse")
        return Failure()
      }

      Logger.debug("Responses match")
      //check the nonce date
      //

    } catch {
      case th: Throwable => {
        Logger.error(th.getMessage, th)
        return Failure()
      }
    }

    return Success(username)
  }

  /**
    * Calculate A1 as described in the RFC7616
    *
    * @param username
    * @param nonce
    * @param cnonce
    */
  def calculateA1(username: String, nonce: String, cnonce: String) = {
    val user = _root_.models.User.findByEmail(username)

    if (user == null) {
      Logger.error(s"No such user $username")
      throw new IllegalArgumentException(s"No such user $username")
    }

    val ha1 = a2h(user.password);

    if (REST_AUTH_DIGEST_ALGORITHM.endsWith("-sess")) {
      Logger.trace("Do alg sess")
      //A1       = H( unq(username) ":" unq(realm) ":" passwd ) ":" unq(nonce-prime) ":" unq(cnonce-prime)
      //H( unq(username) ":" unq(realm) ":" passwd ) is already calculated and is in user.ha1
      //this is A1

      s"${ha1}:$nonce:$cnonce"
    } else {
      Logger.trace("No sess, just HA1")
      //just return ha1
      //A1       = unq(username) ":" unq(realm) ":" passwd
      ha1
    }
  }
}

/**
  * Diger nesnelerin de erişebileceği global sabitleri tutar
  */
object RestAuthUtil {
  var REALM = "suna@suna.com.tr"
  var REALM_BYTES = REALM.getBytes()
  val WWW_AUTHENTICATE = "WWW-Authenticate"
  val AUTHORIZATION = "Authorization"
  var REST_AUTH_DIGEST_ALGORITHM = "MD5"
}