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
	@JoinColumn(name="ID")
	public User user;
	
	@OneToOne(mappedBy = "userHistory",fetch=FetchType.LAZY)
	public TOperationType operationType;
	
	@Column(name = "LOG")
	public String systemOutputLog;
}

