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
	
	@Column(name = "GUID", nullable = false, updatable = false, unique = true)
	public String GUID;
	
	@Column(name = "NAME")
	public String name;
	
	@ManyToOne
	@JoinColumn(name="ID")	
	public User user;
	
	@OneToMany(mappedBy = "wrapper", fetch=FetchType.EAGER,cascade = CascadeType.ALL)
	public List<Signal> signals;
	
	@OneToMany(mappedBy = "wrapper", fetch=FetchType.EAGER,cascade = CascadeType.ALL)
	public List<Slot> slots;

	public void setGUID(String gUID) {
		//GUID genertaion burada
		GUID = gUID;
	}
}
