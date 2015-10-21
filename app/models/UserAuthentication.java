package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.SqlUpdate;
import play.Logger;
import play.data.format.Formats;
import rest.controllers.common.Constants;
import rest.controllers.common.RestUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 1/10/15.
 */
@Entity
@Table(name = "UserAuthentication")
public class UserAuthentication extends Model {

    public UserAuthentication() {
    }


    @Id
    public Long id;

    @ManyToOne
    public User user;

    public String realm;

    /*nonce: server side, which is generated in each authentication request*/
    public String serverNonce;

    /*nonce issue Time */
    @Formats.DateTime(pattern = Constants.DATE_FORMAT)
    public Date issueTime;

    /*for nonce to check its expiry*/
    @Formats.DateTime(pattern = Constants.DATE_FORMAT)
    public Date expiryTime;

    /*nc:request counter*/
    public int requestCounter;

    private static final Finder<Long, UserAuthentication> find = new Finder<>(UserAuthentication.class);


    public static UserAuthentication create(String userEmail, String realm, String serverNonce, int requestCounter) {
        final UserAuthentication userAuthentication = new UserAuthentication();
        userAuthentication.user = User.findByEmail(userEmail);
        userAuthentication.serverNonce = serverNonce;
        userAuthentication.requestCounter = requestCounter;
        userAuthentication.realm = realm;
        userAuthentication.issueTime = new Date();

        long issueTimeInMilis=userAuthentication.issueTime.getTime();
        long totalTimeToValidity = issueTimeInMilis + Constants.NONCE_VALIDITY_MILISECONDS;
        userAuthentication.expiryTime = RestUtils.getDate(totalTimeToValidity);

        userAuthentication.save();
        return userAuthentication;
    }

    public static void update(String serverNonce, int newRequestCounter) {
        final UserAuthentication userAuthentication = findByServerNonce(serverNonce);
        userAuthentication.requestCounter = newRequestCounter;

        userAuthentication.update();
    }

    public static UserAuthentication findByUserEmail(final String userEmail) {

        User user = User.findByEmail(userEmail);
        return find.where().eq("user",user).findUnique();
    }

    public static UserAuthentication findByServerNonceAndRealm(final String realm,final String serverNonce) {
        return find.where().eq("serverNonce", serverNonce).eq("realm",realm).findUnique();
    }

    public static UserAuthentication findByServerNonce(final String serverNonce) {

        return find.where().eq("serverNonce", serverNonce).findUnique();
    }

    public void updateAuthenticationRecord(String realm, String serverNonce, String userEmail, String clientNonce,String requestedURI,int requestCounter) {
        UserAuthentication userAuthentication =  findByServerNonceAndRealm(realm,serverNonce);

        userAuthentication.user = User.findByEmail(userEmail);
        userAuthentication.requestCounter = requestCounter;
        userAuthentication.issueTime = new Date();

        long issueTimeInMilis=userAuthentication.issueTime.getTime();
        long totalTimeToValidity = issueTimeInMilis + Constants.NONCE_VALIDITY_MILISECONDS;
        userAuthentication.expiryTime = RestUtils.getDate(totalTimeToValidity);

        userAuthentication.save();
    }

    public static int deleteAllByUserEmail(String userEmail) {
        User user = User.findByEmail(userEmail);

        Logger.debug("Delete all user authentication records" + userEmail);

        SqlUpdate tangoDown = Ebean.createSqlUpdate("DELETE FROM UserAuthentication WHERE user=:user");
        final int i = tangoDown.execute();

        Logger.debug(i + " user authentication records deleted");
        return i;
    }

    public static int deleteByUserEmailAndNonce(String userEmail, String serverNonce) {
        User user = User.findByEmail(userEmail);

        Logger.debug("Delete user authentication record " + userEmail +" "+serverNonce);

        SqlUpdate tangoDown = Ebean.createSqlUpdate("DELETE FROM UserAuthentication WHERE user=:+"+user+" AND serverNonce ="+ serverNonce);
        final int i = tangoDown.execute();

        Logger.debug(i + " user authentication record deleted");
        return i;
    }

    public static int deleteAllExpiredNonces() {
        Logger.debug("Delete All Expired Nonces");

        String sqlWord = "DELETE FROM UserAuthentication WHERE expiry_time < NOW() - INTERVAL '" + Constants.NONCE_VALIDITY_IN_DB+"'";
        SqlUpdate tangoDown = Ebean.createSqlUpdate(sqlWord);
        final int i = tangoDown.execute();

        Logger.debug(i + " user authentication record deleted");
        return i;
    }


}
