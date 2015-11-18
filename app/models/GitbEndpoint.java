package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;

@Entity
@Table(name = "GitbEndpoint")
public class GitbEndpoint extends Model {

	@Id
	public Long id;

	@Column(name = "name", nullable = false)
	public String name;
	
	@Column(length = ModelConstants.DESCRIPTION_LENGTH, columnDefinition = "TEXT")
	public String description;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<GitbParameter> params;
	
	private static final Finder<Long, GitbEndpoint> find = new Finder<>(GitbEndpoint.class);

	public static GitbEndpoint findById(Long id) {
		return find.byId(id);
	}
}
