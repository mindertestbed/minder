package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import controllers.common.enumeration.OperationType;
import com.avaje.ebean.Model;

@Entity
@Table(name = "OperationType")
public class TOperationType extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;
	
	@Column(name = "OPERATION_TYPE")
	@Enumerated(EnumType.STRING)
	public OperationType name;
	
}
