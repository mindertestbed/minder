package global

import java.util._

import play.Logger
import play.data.Form
import play.data.validation.ValidationError

/**
 * Created by yerlibilgin on 02/05/15.
 */
object Util {
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
}
