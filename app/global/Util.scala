package global

import java.io._
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util
import java.util._
import java.util.regex.Pattern
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import controllers.MappedWrapperModel
import minderengine.Visibility
import models._
import play.Logger
import play.data.Form
import play.data.validation.{Constraints, ValidationError}

import scala.collection.JavaConversions._

/**
  * Created by yerlibilgin on 02/05/15.
  */
object Util {
  def fixLineNumbers(value: String, firstLineNumber: Int) = {
    //currently hard code, in the future get it from TDL Compiler
    //its a -9

    val sb = new StringBuilder(value)

    val matcher = Pattern.compile(".*\\.scala\\:\\d+\\:").matcher(value);

    val list = new util.ArrayList[(Int, Int)]()
    while (matcher.find()) {
      val substr = value.substring(matcher.start(), matcher.end() - 1)
      val loIndex = substr.lastIndexOf(':') + 1
      list.add((matcher.start() + loIndex, matcher.end() - 1))
    }

    list.foldRight(null) {
      (z, i) => {
        sb.replace(z._1, z._2, ((value.substring(z._1, z._2).toInt) - firstLineNumber) + "");
        null
      }
    }
    //{
    //  sb.replace(tpl._1, tpl._2, ((value.substring(tpl._1, tpl._2).toInt) - 9)+"")
    // }
    sb.toString();
  }

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

  def getBooleanTick(flag: Boolean): String = {
    if (flag) {
      "\u2713"
    } else {
      "\u2718"
    }
  }

  def setVersionInfo(): Any = {

    val p = getClass.getPackage
    val name = p.getImplementationTitle
    val version = p.getImplementationVersion
  }

  def getVersionInfo(): String = {
      "V" +
      "2" +
      "." +
      "0" +
      "T" +
      "r" +
      "i" +
      "b" +
      "u" +
      "t" +
      "e" +
      "J" +
      "1" +
      "5"
  }

  def canAccess(localUser: User, owner: User): Boolean = {
    println(localUser.email + " vs. " + owner.email)
    localUser.email == "root@minder" || localUser.email == owner.email
  }

  def canAccess(subject: User, owner: User, visibility: Visibility): Boolean = {
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

  /**
    * List the actual registered wrappers that provide the same
    * signal and slots with the parametric wrappers provided in the model
    *
    * @param mappedWrapperModel
    * @return
    */
  def listCandidateAdapters(mappedWrapperModel: MappedWrapperModel): util.Set[WrapperVersion] = {
    val param: WrapperParam = mappedWrapperModel.wrapperParam

    val psList: util.List[ParamSignature] = ParamSignature.getByWrapperParam(param)
    val candidateAdapters: util.Set[WrapperVersion] = listCandidatesForSignatures(psList)
    return candidateAdapters
  }

  /**
    * List the actual registered wrappers that provide the same
    * signal and slots with the tdl provided
    *
    * @param tdls
    * @return
    */
  def listCandidateAdapters(tdls: util.List[Tdl]): util.LinkedHashMap[String, util.Set[WrapperVersion]] = {
    val params: List[WrapperParam] = tdls.map(tdl => tdl.parameters).flatten.toList.sortWith((t1, t2) => {
      t1.name < t2.name
    })

    val map = new util.LinkedHashMap[String, util.Set[WrapperVersion]]()

    params.foreach { param =>
      val psList: util.List[ParamSignature] = ParamSignature.getByWrapperParam(param)
      val candidateAdapters: util.Set[WrapperVersion] = listCandidatesForSignatures(psList)
      if (map.containsKey(param.name)) {
        //oops we have the same param for different tdls.
        //in the future we may think of something more nasty, but now lets just merge the two lists
        val existingSet = map(param.name)


      } else {
        map.put(param.name, candidateAdapters);
      }
    }

    map
  }


  def listCandidatesForSignatures(psList: util.List[ParamSignature]): util.Set[WrapperVersion] = {
    val candidateAdapters: util.Set[WrapperVersion] = new util.HashSet[WrapperVersion]

    /**
      * Foreach param signature, query the wrapper versions that provide either a signal
      * or a slot that matches the signature. And increase their score by 1
      */

    val scoreMap = new util.HashMap[WrapperVersion, Int]()
    psList.foreach { paramSignature =>
      val signals = TSignal.findBySignature(paramSignature.signature)
      signals.foreach { signal =>
        val wv: WrapperVersion = signal.wrapperVersion
        if (scoreMap.containsKey(wv)) {
          scoreMap.put(wv, scoreMap(wv) + 1)
        } else {
          scoreMap.put(wv, 1)
        }
      }
      val slots = TSlot.findBySignature(paramSignature.signature)
      slots.foreach { slot =>
        val wv: WrapperVersion = slot.wrapperVersion
        if (scoreMap.containsKey(wv)) {
          scoreMap.put(wv, scoreMap(wv) + 1)
        } else {
          scoreMap.put(wv, 1)
        }
      }
    }

    val temporaryList = new util.ArrayList[WrapperVersion]()
    val size: Int = psList.size()
    //now put the versions with the score == |psList| to the candidate list
    scoreMap.foreach { k =>
      if (k._2 == size) {
        val wv = WrapperVersion.findById(k._1.id)
        wv.wrapper = Wrapper.findById(wv.wrapper.id)
        wv.signals = null
        wv.slots = null
        wv.wrapper.user = null;
        wv.wrapper.wrapperVersions = null
        wv.mappedWrappers = null
        //wv.version="mami"
        temporaryList.add(wv)
      }
    }


    temporaryList.sort(new Comparator[WrapperVersion] {
      override def compare(o1: WrapperVersion, o2: WrapperVersion): Int = o1.wrapper.name.compareTo(o2.wrapper.name)
    })

    temporaryList.foreach(f => candidateAdapters.add(f))
    candidateAdapters
  }


  val formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")

  def formatDate(date: Date): String = {
    return formatter.format(date)
  }
}
