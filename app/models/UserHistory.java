package models;

import javax.persistence.*;

import play.db.ebean.Model;

@Entity
@Table(name = "UserHistory")
public class UserHistory extends Model {
	@Id
	public Long id;

	public String email;
	
	@OneToOne(fetch=FetchType.EAGER)
	public TOperationType operationType;
	
	@Column(name = "LOG", length = ModelConstants.LOG_LENGTH)
	public String systemOutputLog;
}

