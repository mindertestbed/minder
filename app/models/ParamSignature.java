package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ParamSignature")
public class ParamSignature extends Model {
	@Id
	public Long id;

	@Column(name = "name")
	public String signature;

	@ManyToOne
	public WrapperParam wrapperParam;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ParamSignature that = (ParamSignature) o;

		if (!signature.equals(that.signature)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + signature.hashCode();
		return result;
	}

	public static final Finder<Long, ParamSignature> find = new Finder<>(Long.class,
			ParamSignature.class);
	
	public static List<ParamSignature> getAll() {
		return find.all();
	}

	public static List<ParamSignature> getByWrapperParam(WrapperParam wp) {
		return find.where().eq("wrapperParam", wp).findList();

	}
}

