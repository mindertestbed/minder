package controllers.persistence

import model.facade._
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.meta.MTable

class DataAccessRegistry(val driver:JdbcProfile) extends UserComponent with DriverComponent {
	import driver.simple._
	
	//Create schemas of all tables
	def create(implicit session:Session) {
	  if (!MTable.getTables.list.exists(_.name.name == "USERS"))
        users.ddl.create
	}
}