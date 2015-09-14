package e2etest

import java.io.FileInputStream
import java.util

import com.avaje.ebean.Ebean
import models.User
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import play.api.test._
import com.avaje.ebean.Model

import scala.collection.JavaConversions._

/**
 * This class performs an end-to-endTes
 * test case running a predefined TDL where
 * an xml-value-initiator, an xml-generator, a content verifier
 * and a reporting tool are used.
 */
@RunWith(classOf[JUnitRunner])
class YamlTetst extends Specification {
  sequential

  "Yaml" should {
    "load" in new WithApplication {
      try {
        val yaml: Yaml = new Yaml(new CustomClassLoaderConstructor(this.getClass.getClassLoader))

        val all = yaml.load(new FileInputStream("conf/initial-data.yml")).asInstanceOf[java.util.LinkedHashMap[String, util.ArrayList[Object]]];

        val set: java.util.Set[String] = all.keySet()

        for (key <- set) {
          println("KEY: " + key)
          val lst = all.get(key)
          println("Value:")
          for (obj <- lst) {
            println(all.get(key))
            println("----")

            var m = obj.asInstanceOf[Model];
            m.save()
          }
        }
      } catch {
        case t: Throwable => {
          t.printStackTrace
        }
      }
    }
  }
}
