package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by yerlibilgin on 30/12/14.
 */
@Entity
@Table(name="TestRun")
public class TestRun extends Model{
  @Id
  public Long id;

  @ManyToOne
  @Column(nullable = false)
  public TestCase testCase;

  public Date date;

  @ManyToOne
  @Column(nullable = false)
  public User runner;

  @OneToOne
  public Log history;

  public TestRun(){

  }

  public TestRun(TestCase testCase, User runner){
    this.testCase = testCase;
    this.runner = runner;
    this.date = new Date();
  }


  public static final Finder<Long, TestRun> find = new Finder<>(
      Long.class, TestRun.class);
}
