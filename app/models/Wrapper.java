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

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<TSignal> signals;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<TSlot> slots;

	public static final Finder<Long, Wrapper> find = new Finder<>(Long.class,
			Wrapper.class);
	
	public static List<Wrapper> getAll() {
		return find.all();
	}
}
