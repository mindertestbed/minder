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

class DatabaseTests {
  @Test
  def databaseInsertTest = {

    running(FakeApplication()) {
        try {
          println("Enter sandman")

          val database = Database.forURL(current.configuration.getString("db.default.url").get,
            driver = current.configuration.getString("db.default.driver").get,
            user = current.configuration.getString("db.default.user").get,
            password = current.configuration.getString("db.default.password").get)

          insertIntoDb(new DataAccessRegistry(PostgresDriver), database)
          println("Exit test")
        } finally Util.unloadDrivers
      }

  }

  def insertIntoDb(dataAccess: DataAccessRegistry, db: Database) {
    import dataAccess.driver.simple._

    println("Running test against " + dataAccess.driver)
    db withSession { implicit session: Session =>
      dataAccess.create

      // Inserting users
      println("- Inserted user: " + dataAccess.insert(User("melis", "123")))
      println("- Inserted user: " + dataAccess.insert(User("ozgur", "4567")))

      println("- All users: " + dataAccess.users.list)
    }
  }

}
