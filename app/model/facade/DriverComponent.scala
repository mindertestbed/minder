package model.facade

import scala.slick.driver.JdbcProfile

trait DriverComponent {
	val driver : JdbcProfile
}