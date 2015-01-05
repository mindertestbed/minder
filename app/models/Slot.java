package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name = "Slot")
public class Slot extends  Model {
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
