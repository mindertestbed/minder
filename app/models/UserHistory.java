package models;

import javax.persistence.*;

import com.avaje.ebean.Model;
import controllers.common.enumeration.OperationType;

import java.io.UnsupportedEncodingException;

@Entity
@Table(name = "UserHistory")
public class UserHistory extends Model {

  @Id
  public Long id;

  public String email;

  @Column(name = "optype")
  public OperationType operationType;

  @Column(name = "SYSLOG", length = ModelConstants.LOG_LENGTH)
  public byte[] systemOutputLog;

  public String extractSystemOutputLog() {
    if (systemOutputLog == null)
      return "";

    return new String(utils.Util.gunzip(systemOutputLog));
  }


  public void setSystemOutputLog(String log) {
    try {
      this.systemOutputLog = utils.Util.gzip(log.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {

    }
  }
}

