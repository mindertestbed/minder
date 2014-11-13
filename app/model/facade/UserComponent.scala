package model.facade

case class User(name: String, password: String, id: Option[Int] = None)

// UserComponent provides database definitions for User objects 
trait UserComponent { this: DriverComponent =>
  import driver.simple._
  //import query language features from the driver

  class Users(tag: Tag) extends Table[(String, String, Option[Int])](tag, "USERS") {
    def id = column[Option[Int]]("USER_ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("USER_MANE", O.NotNull)
    def password = column[String]("USER_PASSWORD", O.NotNull)
    def * = (name, password, id)
  }
  val users = TableQuery[Users]

  private val usersAutoInc =
    users.map(u => (u.name, u.password)) returning users.map(_.id)

  //Insert a record
  def insert(user: User)(implicit session: Session) = {
    val id = usersAutoInc.insert(user.name, user.password)
   // users += (user.name, user.password)
  //users.map(user => (user.name, user.password))
  }
  // Get the password for the given name */
  def get(name: String)(implicit session: Session): Option[String] =
    (for (u <- users if u.name === name) yield u.password).firstOption

  //Delete a record
  //def delete(user: User)(implicit session:Session)=
  //  users.filter(_.name=name).delete

  //Update a record

}