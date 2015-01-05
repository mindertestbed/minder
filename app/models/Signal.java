package models;

import javax.persistence.*;

import play.db.ebean.Model;

@Entity
@Table(name = "Signal")
public class Signal extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	public Long id;
	
	@Column(name = "SIGNATURE", nullable = false, updatable = false, unique = true)

	public String signature;
	
	@ManyToOne
	@JoinColumn(name="ID")	
	public Wrapper wrapper;
	
}
