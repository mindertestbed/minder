
package rest.models.runModel;

import models.TestRunStatus;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for runStatus.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="runStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IDLE"/>
 *     &lt;enumeration value="CANCELLED"/>
 *     &lt;enumeration value="IN_PROGRESS"/>
 *     &lt;enumeration value="SUCCESS"/>
 *     &lt;enumeration value="FAIL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "runStatus")
@XmlEnum
public enum RunStatus {

  IDLE,
  CANCELLED,
  IN_PROGRESS,
  SUCCESS,
  FAIL;

  public String value() {
    return name();
  }

  public static RunStatus fromValue(String v) {
    return valueOf(v);
  }

  public static RunStatus mapFrom(TestRunStatus testRunStatus) {
    switch (testRunStatus) {
      case FAILED:
        return FAIL;
      case PENDING:
        return IDLE;
      case SUCCESS:
        return SUCCESS;
      case CANCELLED:
        return CANCELLED;
      case IN_PROGRESS:
        return IN_PROGRESS;
    }
    return null; //impossible
  }

}
