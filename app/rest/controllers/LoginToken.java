package rest.controllers;

import rest.controllers.common.Utils;

import java.util.Date;

/**
 * Created by melis on 16/10/15.
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
        this.issueTime = Utils.getCurrentDate();
    }
}
