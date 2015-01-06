package models;

import javax.persistence.*;

import play.db.ebean.Model;

@Entity
@Table(name = "UserHistory")
public class UserHistory extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	public Long id;
	
	@ManyToOne
	public User user;
	
	@OneToOne(fetch=FetchType.EAGER)
	public TOperationType operationType;
	
	@Column(name = "LOG")
	public String systemOutputLog;
}

