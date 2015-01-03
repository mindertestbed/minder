package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Permission;

@Entity
@Table(name = "UserPermission")
public class UserPermission extends Model implements Permission {

  @Id
  public Long id;

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public String value;

  public static final Model.Finder<Long, UserPermission> find = new Model.Finder<Long, UserPermission>(
      Long.class, UserPermission.class);

  public String getValue() {
    return value;
  }

  public static UserPermission findByValue(String value) {
    return find.where().eq("value", value).findUnique();
  }
}
