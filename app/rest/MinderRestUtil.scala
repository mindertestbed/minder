package rest

import java.io._
import java.text.SimpleDateFormat
import java.util._
import javax.xml.bind.DatatypeConverter
import javax.xml.datatype.{DatatypeFactory, XMLGregorianCalendar}

/**
  */
object MinderRestUtil {

  def prepareResults(seq: (String, Any)*) = {
    val sb = new StringBuilder("")

    seq.foreach { tpl =>
      sb.append("  <").append(tpl._1).append('>')
          .append(if (tpl._2.isInstanceOf[Array[Byte]]) {
            toBase64(tpl._2.asInstanceOf[Array[Byte]])
          } else {
            tpl._2.toString
          })
          .append("</").append(tpl._1).append(">\n")
    }

    sb.toString()
  }

  def md5(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("MD5");
    md.digest(array)
  }

  def sha256(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("SHA-256");
    md.digest(array)
  }

  def sha512(array: Array[Byte]): Array[Byte] = {
    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("SHA-512");
    md.digest(array)
  }


  /**
    * Boş digest metodu. Configurasyondan okunacak olan DIGEST parametresine göre
    * MD5 SHA-256 veya SHA-512 ye set edilir.
    */
  var digest: String => String = null


  /**
    * verilen hash tipine gore digest metodunu dekore eder
    *
    * @param method MD5, SHA-256 veya SHA-512
    */
  def setSigestMethod(method: String): Unit = {
    method match {
      case "MD5" => digest = str => a2h(md5(str.getBytes))
      case "SHA-256" => digest = str => a2h(sha256(str.getBytes))
      case "SHA-512" => digest = str => a2h(sha256(str.getBytes))

    }
  }

  def readResource(s: String): Array[Byte] = {
    val inputStream = this.getClass.getResourceAsStream(s)
    val baos = new ByteArrayOutputStream()
    val array = new Array[Byte](1024)
    var read = inputStream.read(array)
    while (read > 0) {
      baos.write(array, 0, read)
      read = inputStream.read(array)
    }

    baos.toByteArray
  }

  def randomUUID(s: String): String = {
    val uuid = UUID.randomUUID().toString

    if (s != null)
      uuid + "@" + s
    else
      uuid
  }

  def currentTimeStamp: XMLGregorianCalendar = {
    val c = new GregorianCalendar();
    c.setTime(new Date());
    DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
  }


  /**
    * Hex string to byte array
    *
    * @param string
    * @return
    */
  def h2a(string: String): Array[Byte] = DatatypeConverter.parseHexBinary(string)

  /**
    *
    * @param array
    * @return
    */
  def a2h(array: Array[Byte]): String = DatatypeConverter.printHexBinary(array).toLowerCase


  def parseBase64(string: String): Array[Byte] = {
    DatatypeConverter.parseBase64Binary(string)
  }

  def toBase64(array: Array[Byte]): String = {
    DatatypeConverter.printBase64Binary(array)
  }

  def shorten(str: String, max: Int) = {
    if (str == null) {
      ""
    } else if (str.length < max) {
      str
    } else {
      str.substring(0, max)
    }
  }


  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def format(date: Date) = sdf.format(date)

}

