package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by yerlibilgin on 07/01/15.
 */
@Entity
@Table(name = "MappedWrapper", uniqueConstraints = @UniqueConstraint(columnNames = {"parameter", "wrapper", "runConfiguration"}))
public class MappedWrapper extends Model{
  @Id
  public Long id;

  @Column(nullable = false)
  public WrapperParam parameter;

  @OneToOne
  public Wrapper wrapper;

  @ManyToOne
  public RunConfiguration runConfiguration;

  public static final Finder<Long, MappedWrapper> find = new Finder<>(Long.class,
      MappedWrapper.class);
}
