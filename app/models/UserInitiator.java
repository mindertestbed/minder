package models;


import com.avaje.ebean.Model;
import global.Util;

/**
 * A placeholder class, only used for initiating the database
 * from the initial-data.yml file. The only purpose is to assure
 * that a human readable (thus easily editable) password is written
 * into the yml file (instead of base 64 encoded password hash)
 */
public class UserInitiator extends Model{
  public String password;

  public User user;
  /**
   * Converts the passwordString into a real password by hashing it.
   */
  @Override
  public void save(){
    user.password = Util.sha256(password.getBytes());
    user.save();
  }
}
