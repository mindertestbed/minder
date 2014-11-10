package minderengine;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeProvider {
  public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm");
  
  public static String getDate(){
    return sdf.format(new Date());
  }
}
