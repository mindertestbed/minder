package global

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

}
