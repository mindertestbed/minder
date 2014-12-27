package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by yerlibilgin on 24/12/14.
 */
@Entity
public class MinderEntity extends Model {
  @Id
  public Integer id;
}
