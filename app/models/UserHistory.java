package models;

import javax.persistence.*;

import play.db.ebean.Model;

import java.io.UnsupportedEncodingException;

@Entity
@Table(name = "UserHistory")
public class UserHistory extends Model {
  @Id
  public Long id;

  public String email;

  @OneToOne(fetch = FetchType.EAGER)
  public TOperationType operationType;

  @Column(name = "SYSLOG", length = ModelConstants.LOG_LENGTH)
  public byte[] systemOutputLog;

  public String extractSystemOutputLog() {
    return new String(global.Util.gunzip(systemOutputLog));
  }


  public void setSystemOutputLog(String log) {
    try {
      this.systemOutputLog = global.Util.gzip(log.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {

    }
  }
}

