package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.SqlUpdate;
import play.Logger;
import play.data.format.Formats;
import rest.controllers.common.Constants;
import rest.controllers.common.Utils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * Created by melis on 13/10/15.
 */
@Entity
@Table(name = "UserAuthentication")
public class UserAuthentication extends Model {

    public UserAuthentication() {
    }


    @Id
    public Long id;

    @ManyToOne
    User user;

    String realm;

    /*nonce: server side, which is generated in each authentication request*/
    String serverNonce;

    /*cnonce:client side, which is generated in each request*/
    String clientNonce;

    String requestedURI;

    /*nonce issue Time */
    @Formats.DateTime(pattern = Constants.DATE_FORMAT)
    public Date issueTime;

    /*for nonce to check its expiry*/
    @Formats.DateTime(pattern = Constants.DATE_FORMAT)
    public Date expiryTime;

    /*nc:request counter*/
    int requestCounter;

    private static final Finder<Long, UserAuthentication> find = new Finder<>(UserAuthentication.class);


    public static UserAuthentication create(String userEmail, String realm, String serverNonce, String clientNonce,String requestedURI, int requestCounter) {
        final UserAuthentication userAuthentication = new UserAuthentication();
        userAuthentication.user = User.findByEmail(userEmail);
        userAuthentication.serverNonce = serverNonce;
        userAuthentication.clientNonce = clientNonce;
        userAuthentication.requestCounter = requestCounter;
        userAuthentication.requestedURI= requestedURI;
        userAuthentication.realm = realm;
        userAuthentication.issueTime = new Date();

        long issueTimeInMilis=userAuthentication.issueTime.getTime();
        long totalTimeToValidity = issueTimeInMilis + Constants.NONCE_VALIDITY_MILISECONDS;
        userAuthentication.expiryTime = Utils.getDate(totalTimeToValidity);

        userAuthentication.save();
        return userAuthentication;
    }

    public static UserAuthentication findByUserEmail(final String userEmail) {

        User user = User.findByEmail(userEmail);
        return find.where().eq("user",user).findUnique();
    }

    public static List<UserAuthentication> findByServerNonceAndRealm(final String realm,final String serverNonce) {

        return find.where().eq("serverNonce", serverNonce).eq("realm",realm).findList();
    }

    public void updateAuthenticationRecord(String realm, String serverNonce, String userEmail, String clientNonce,String requestedURI,int requestCounter) {
        UserAuthentication userAuthentication =  findByServerNonceAndRealm(realm,serverNonce).get(0);

        userAuthentication.user = User.findByEmail(userEmail);
        userAuthentication.clientNonce = clientNonce;
        userAuthentication.requestedURI = requestedURI;
        userAuthentication.requestCounter = requestCounter;
        userAuthentication.issueTime = new Date();

        long issueTimeInMilis=userAuthentication.issueTime.getTime();
        long totalTimeToValidity = issueTimeInMilis + Constants.NONCE_VALIDITY_MILISECONDS;
        userAuthentication.expiryTime = Utils.getDate(totalTimeToValidity);

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
