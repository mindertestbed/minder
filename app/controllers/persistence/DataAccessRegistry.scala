package controllers.persistence

import model.facade._
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.meta.MTable
import controllers.common.Constants;

class DataAccessRegistry(val driver: JdbcProfile) extends UserComponent with DriverComponent {
  import driver.simple._

  //Create schemas of all tables
  def create(implicit session: Session) {
    if (!MTable.getTables.list.exists(_.name.name == Constants.TABLE_NAME_USER))
      users.ddl.create
  }

  //Drop and Create schemas of all tables
  def dropAndCreate(implicit session: Session) {
    users.ddl.drop
    users.ddl.create
  }
}