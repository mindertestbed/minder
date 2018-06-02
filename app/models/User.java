package models;

import com.avaje.ebean.Model;
import utils.UserPasswordUtil;
import utils.Util;
import play.data.format.Formats;
import play.data.validation.Constraints;
import security.Role;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Users")
public class User extends Model {
  @Id
  public Long id;

  @Constraints.Email
  @Column(unique = true)
  public String email;

  public String name;

  @Constraints.Required
  public byte[] password;

  @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
  public Date lastLogin;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  public List<DBRole> roles;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  public List<Adapter> adapters;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "owner")
  public List<TestGroup> testGroups;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "owner")
  public List<TestAssertion> testAssertions;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "owner")
  public List<TestCase> testCases;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "owner")
  public List<AbstractJob> jobs;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "owner")
  public List<TestSuite> suites;

  private static final Finder<Long, User> find = new Finder<>(User.class);

  public List<? extends DBRole> getRoles() {
    return roles;
  }

  public static List<User> findAll() {
    return find.where().orderBy("email").findList();
  }


  public boolean isTester() {
    for (DBRole role : roles) {
      if (role.role == Role.TEST_DESIGNER) {
        return true;
      }
    }
    return false;
  }


  public boolean hasRole(Role role) {
    for (DBRole role2 : roles) {
      if (role2.role == role) {
        return true;
      }
    }
    return false;
  }

  public boolean isDeveloper() {
    for (DBRole role : roles) {
      if (role.role == Role.TEST_DEVELOPER) {
        return true;
      }
    }
    return false;
  }


  public boolean isObserver() {
    for (DBRole role : roles) {
      if (role.role == Role.TEST_OBSERVER) {
        return true;
      }
    }

    return false;
  }

  public static User create(String email, String name, String password, Role... roles) {
    final User user = new User();
    user.lastLogin = new Date();
    user.name = name;
    user.email = email;
    user.roles = new ArrayList<>();
    user.password = Util.sha256(password.getBytes());
    for (Role role : roles) {
      user.roles.add(new DBRole(user, role));
    }
    user.save();
    return user;
  }

  public static User findByEmail(final String email) {
    return find.where().eq("email", email).findUnique();
  }

  public static User findById(Long id) {
    return find.byId(id);
  }

  @Override
  public String toString() {
    return email;
  }

  public static int findRowCount() {
    return find.findRowCount();
  }

  /**
   * Created for ease of use in the initialization YML file
   * @param plainPassword
   */
  public void setPlainPassword(String plainPassword) {
    this.password = UserPasswordUtil.generateHA1(email, plainPassword);
  }
}
