package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.Update;
import play.Logger;
import security.Role;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author: yerlibilgin
 * @date: 29/08/15.
 */
@Entity
public class DBRole extends Model {

  public DBRole() {
  }

  public DBRole(User user, Role role) {
    this.user = user;
    this.role = role;
  }

  @Id
  public Long id;

  @ManyToOne
  public User user;

  public Role role;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DBRole dbRole = (DBRole) o;

    return role == dbRole.role;

  }

  @Override
  public int hashCode() {
    if (role == null)
      return super.hashCode();

    return role.hashCode();
  }

  public static int deleteAllByUser(User user) {
    Logger.debug("Delete roles of " + user);
    Update<DBRole> upd = Ebean.createUpdate(DBRole.class, "DELETE from DBRole WHERE user=:user");
    upd.set("user", user.id);
    final int i = upd.execute();
    Logger.debug(i + " roles deleted");
    return i;
  }
}
