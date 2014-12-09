package minderengine


import mtdl.MinderTdl

import scala.reflect.runtime._
import scala.tools.reflect.ToolBox

/**
 * This class provides means to run a tdl defined test
 */
object TestCaseRunner {
  def main(args: Array[String]): Unit ={
    var te = new TestEngine

    te.compileTdl(null)
  }
}
