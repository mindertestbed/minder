package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@Table(name="Log")
public class Log extends Model{
  @Id
  public Long id;

  @ManyToOne
  public User user;

  @Column(length = 10000)
  public String log;


  public static final Finder<Long, Log> find = new Finder<>(
      Long.class, Log.class);
}
