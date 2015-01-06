package models;

import java.util.List;

import javax.persistence.*;

import play.db.ebean.Model;

@Entity
@Table(name = "Wrapper")
public class Wrapper extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	@Column(name = "NAME", unique = true)
	public String name;

	@ManyToOne
	public User user;

	@OneToMany(mappedBy = "wrapper", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<TSignal> signals;

	@OneToMany(mappedBy = "wrapper", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<TSlot> slots;

	public static final Finder<Long, Wrapper> find = new Finder<>(Long.class,
			Wrapper.class);
}
