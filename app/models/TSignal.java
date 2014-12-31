package models;

import play.db.ebean.Model;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by yerlibilgin on 31/12/14.
 */
public class TSignal extends Model {
  @Id
  public Long id;

  @ManyToOne
  public Wrapper wrapper;

  public String signature;


  public static final Finder<Long, TSignal> find = new Finder<>(
      Long.class, TSignal.class);
}
