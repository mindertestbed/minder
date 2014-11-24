package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Permission;

@Entity
public class UserPermission extends Model implements Permission {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String value;

	public static final Model.Finder<Long, UserPermission> find = new Model.Finder<Long, UserPermission>(
			Long.class, UserPermission.class);

	public String getValue() {
		return value;
	}

	public static UserPermission findByValue(String value) {
		return find.where().eq("value", value).findUnique();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
