package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.Model;

@Entity
@Table(name = "GitbParameter")
public class GitbParameter extends Model{

	@Id
	public Long id;

	@Column(name = "name", nullable = false)
	public String name;

	@Column(name = "value", nullable = false)
	public String value;

	public GitbUsageEnumeration use;

	public GitbConfigurationType kind;

	@Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
	public String description;

	private static final Finder<Long, GitbParameter> find = new Finder<>(GitbParameter.class);

	public static GitbParameter findById(Long id) {
		return find.byId(id);
	}
}
