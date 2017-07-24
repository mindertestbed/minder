package utils;

import rest.controllers.common.Constants;

/**
 * @author: Yerlibilgin
 * @date: 21/07/17.
 */
public class UserPasswordUtil {
  public static byte[] generateHA1(String email, String password) {
    return Util.md5((email + ":" + Constants.MINDER_REALM + ":" + password).getBytes());
  }
}
