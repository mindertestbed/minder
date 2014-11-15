package database

import scala.slick.driver.PostgresDriver
import scala.slick.jdbc.JdbcBackend.Database
import org.junit.Test
import controllers.common.Util
import controllers.persistence.DataAccessRegistry
import model.facade.User
import play.api.Play.current
import play.api.test.FakeApplication
import play.api.test.Helpers.running
import org.junit.runner._
import org.specs2.runner._
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import minderengine.TimeProvider

@RunWith(classOf[JUnitRunner])
class DatabaseTests extends Specification {
  "Application" should {
    "insert into and retrieve data from the database" in new WithApplication {
      try {

        println("DO Test")
        val database = Database.forURL(current.configuration.getString("db.default.url").get,
          driver = current.configuration.getString("db.default.driver").get,
          user = current.configuration.getString("db.default.user").get,
          password = current.configuration.getString("db.default.password").get)

        //        val dataAcessInstance = { println("Create new dataAcess"); new DataAccessRegistry(PostgresDriver) }
        val dataAcessInstance = { println("Create new dataAcess"); new DataAccessRegistry(PostgresDriver) }
        //        implicit dataAccess: DataAccessRegistry => dataAcessInstance
        insertIntoDb(dataAcessInstance, database)
        retrieveFromDb(dataAcessInstance, database)

        println("Exit test")
      } catch {
        case t: Throwable => t.printStackTrace(); throw t
      } finally Util.unloadDrivers
    }
  }

  def insertIntoDb(dataAccess: DataAccessRegistry, db: Database) {
    import dataAccess.driver.simple._
    println("start")
    println("Running test against " + dataAccess.driver)
    db withSession { implicit session: Session =>
      dataAccess.create

      // Inserting users
      println("- Inserted user: " + dataAccess.insert(User("melis", "123")))
      println("- Inserted user: " + dataAccess.insert(User("ozgur", "4567")))

      println("- All users: " + dataAccess.users.list)
    }
  }

  def retrieveFromDb(dataAccess: DataAccessRegistry, db: Database) {
    import dataAccess.driver.simple._

    println("Running test against " + dataAccess.driver)
    db withSession {
      //EDIT: MUHAMMET
      //This implicit session is actually a parameter to our dataAccess.insert function.
      //here we define the implicit parameter and use it in other functions
      //without explicitly passing it as an argument
      implicit session: Session =>
        dataAccess.create

        //retrieve users
        val tuple = Tuple2(dataAccess.get("melis"), dataAccess.get("ozgur"))

        println(tuple)
        (tuple _1) must beSome("123")
        (tuple _2) must beSome("4567")
    }
  }
}
