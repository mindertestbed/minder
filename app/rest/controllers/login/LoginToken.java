package rest.controllers.login;

import rest.controllers.common.RestUtils;

import java.util.Date;

/**
 * This structure holds the current generated which resides in the hash map currentServerNonces(See RestUtils class.)
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 */
public class LoginToken {
    private String nonce;
    private String realm;
    private Date issueTime;

    public String getNonce() {
        return nonce;
    }

    public String getRealm() {
        return realm;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public LoginToken(String nonce, String realm) {
        this.nonce = nonce;
        this.realm = realm;
        this.issueTime = RestUtils.getCurrentDate();
    }
}
