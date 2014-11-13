package database

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import controllers.persistence.DataAccessRegistry
import scala.slick.driver.{ H2Driver, PostgresDriver }
import scala.slick.driver.PostgresDriver.simple._
import org.postgresql.Driver;
import scala.slick.jdbc.JdbcBackend.Database
import controllers.common.Util
import model.facade.User

//@RunWith(classOf[JUnitRunner])
object DatabaseTests extends App {

  def databaseInsertTest(dataAccess: DataAccessRegistry, db: Database) {
    import dataAccess.driver.simple._

    println("Running test against " + dataAccess.driver)
    db withSession { implicit session: Session =>
      dataAccess.create

      // Inserting users
      println("- Inserted user: " + dataAccess.insert(User("muham", "123")))
      println("- Inserted user: " + dataAccess.insert(User("met", "4567")))

      println("- All users: " + dataAccess.users.list)
    }
  }

  try {
    println("Enter sandman")

    val database = Database.forURL("jdbc:postgresql://localhost:5432/kanepe",
      driver = "org.postgresql.Driver",
      user = "minderlord",
      password = "12345")

    databaseInsertTest(new DataAccessRegistry(PostgresDriver), database)
    println("Exit test")
  } finally Util.unloadDrivers
}
