package global

import java.io._
import java.lang.reflect.Field
import java.util._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import models.{PrescriptionLevel, User, Visibility}
import play.Logger
import play.data.Form
import play.data.validation.{Constraints, ValidationError}
import play.mvc.Http


/**
  * Created by yerlibilgin on 02/05/15.
  */
object Util {

  def feedWidth = 3
  def remaining = 9

  def choose(value: Any, expected: Any, matchValue: String = "activetab", nonMatchValue: String = "passivetab"): String = {
    if (value == expected)
      matchValue
    else
      nonMatchValue
  }

  /*
   * test case CRUD
   */
  def printFormErrors(filledForm: Form[_]) {
    val errors: Map[String, List[ValidationError]] = filledForm.errors
    val set: Set[String] = errors.keySet
    import scala.collection.JavaConversions._
    for (key <- set) {
      Logger.error("KEY")
      import scala.collection.JavaConversions._
      for (ve <- errors.get(key)) {
        Logger.error("\t" + ve.key + ": " + ve.message)
      }
    }
  }


  def checkField(cls: Class[_], fieldName: String, newValue: String): Unit = {
    System.out.println("Class Name" + cls);
    System.out.println("Field Name" + fieldName);
    val fld: Field = cls.getDeclaredField(fieldName)
    val required: Boolean = fld.getAnnotation(classOf[Constraints.Required]) != null

    var minLength: Long = 0
    var maxLength: Long = 0

    val minLenAnnotation: Constraints.MinLength = fld.getAnnotation(classOf[Constraints.MinLength])

    if (minLenAnnotation != null) {
      minLength = minLenAnnotation.value
    }

    val maxLenAnnotation: Constraints.MaxLength = fld.getAnnotation(classOf[Constraints.MaxLength])

    if (maxLenAnnotation != null) {
      maxLength = maxLenAnnotation.value
    }


    println(required + " " + minLength + " " + maxLength + " [" + newValue + "]")

    if (required && (newValue == null || newValue.isEmpty)) {
      throw new IllegalArgumentException(fieldName + " is required. Cannot be empty!.")
    }


    if (minLength > 0 && (newValue == null || newValue.length < minLength)) {
      throw new IllegalArgumentException(fieldName + " length cannot be less than " + minLength)
    }


    if (maxLength > 0 && (newValue == null || newValue.length > maxLength)) {
      throw new IllegalArgumentException(fieldName + " length cannot be more than " + maxLength)
    }
  }

  abstract class Converter {
    def convert(any: Any): Any;
  }

  case class PrescriptionLevelConverter() extends Converter {
    override def convert(any: Any): Any = {
      PrescriptionLevel.valueOf(any.toString);
    }
  }

  val converters = scala.collection.immutable.Map("PrescriptionLevel" -> PrescriptionLevelConverter());

  def convertValue(converter: String, newValue: String): Any = {
    converters(converter).convert(newValue)
  }


  def getBooleanIcon(flag: Boolean): String = {
    if (flag) {
      "/images/Happy-32.png"
    } else {
      "/images/Sad-32.png"
    }
  }


  def canAccess(localUser: User, owner: User): Boolean = {
    println(localUser.email + " vs. " + owner.email)
    localUser.email == "root@minder" || localUser.email == owner.email
  }

  def canAccess(subject: User, owner: User, visibility: models.Visibility): Boolean = {
    if (subject != null && owner != null) {
      if ((subject.email == "root@minder") || (subject.email == owner.email) ||
        (subject.hasRole(security.Role.TEST_DESIGNER) && visibility != Visibility.PRIVATE)
        || (visibility == Visibility.PUBLIC))
        return true
    }
    false
  }

  /**
    * compress the given byte array
    *
    * @param plain
    * @return
    */
  def gzip(plain: Array[Byte]): Array[Byte] = {
    val bais: ByteArrayInputStream = new ByteArrayInputStream(plain)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    gzip(bais, baos)
    return baos.toByteArray
  }

  def gunzip(compressed: Array[Byte]): Array[Byte] = {
    val bais: ByteArrayInputStream = new ByteArrayInputStream(compressed)
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    gunzip(bais, baos)
    return baos.toByteArray
  }

  /**
    * Compress the given stream as GZIP
    *
    * @param inputStream
    * @param outputStream
    */
  def gzip(inputStream: InputStream, outputStream: OutputStream) {
    try {
      val gzipOutputStream: GZIPOutputStream = new GZIPOutputStream(outputStream, true)
      transferData(inputStream, gzipOutputStream)
      gzipOutputStream.close
    }
    catch {
      case e: Exception => {
        throw new RuntimeException("GZIP Compression failed")
      }
    }
  }

  /**
    * Decompress the given stream that contains gzip data
    *
    * @param inputStream
    * @param outputStream
    */
  def gunzip(inputStream: InputStream, outputStream: OutputStream) {
    try {
      val gzipInputStream: GZIPInputStream = new GZIPInputStream(inputStream)
      transferData(gzipInputStream, outputStream)
      gzipInputStream.close
    }
    catch {
      case e: Exception => {
        throw new RuntimeException("GZIP decompression failed")
      }
    }
  }

  @throws(classOf[Exception])
  def transferData(gzipInputStream: InputStream, outputStream: OutputStream) {
    val chunk: Array[Byte] = new Array[Byte](1024)
    var read: Int = -1
    while ((({
      read = gzipInputStream.read(chunk, 0, chunk.length);
      read
    })) > 0) {
      outputStream.write(chunk, 0, read)
    }
  }

  def sha256(array: Array[Byte]): Array[Byte] = {

    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("SHA-256");
    md.digest(array)
  }

  def md5(array: Array[Byte]): Array[Byte] = {

    import java.security.MessageDigest;
    val md = MessageDigest.getInstance("MD5");
    md.digest(array)
  }

  def compareArray(array1: Array[Byte], array2: Array[Byte]): Boolean = {
    if (array1 == null && array2 == null) {
      true
    } else if (array1 == null) {
      false
    } else if (array2 == null) {
      false
    } else {
      if (array1.length != array2.length)
        false
      else {
        try {
          for (i <- 0 until array1.length) {
            if (array1(i) != array2(i))
              throw new RuntimeException()
          }
          true
        } catch {
          case _: Throwable =>
            false
        }
      }
    }
  }
}
