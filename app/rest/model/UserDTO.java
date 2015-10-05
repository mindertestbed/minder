package rest.model;

import security.Role;

import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 23/09/15.
 */
public class UserDTO extends  BaseDTO{
  public String userName;
  public List<Role> roles;
}
