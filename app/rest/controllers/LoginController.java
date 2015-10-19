package rest.controllers;


import models.User;
import models.UserAuthentication;
import play.mvc.Controller;
import play.mvc.Result;
import rest.controllers.common.Constants;
import rest.controllers.common.Utils;
import rest.controllers.common.enumeration.MethodType;

import java.util.HashMap;

/**
 * @author: yerlibilgin
 * @date: 13/10/15.
 */
public class LoginController extends Controller {

    /**
     * 1.Client requests to authenticate the Minder
     * 2.Minder returns realm and nonce to Client with the response code 401.
     *
     * @return 401 Unauthorized
     */
    public static Result login() {
        /// TODO:check the ACCEPT-TYPE header,
        // if the clients wants application/xml then send xml
        // if  ... ... . .... . application/json then send json

        String generatedNonce = Utils.generateNonce();//"488E54E5CBA86B7E094B1C8DD6D53602";
        LoginToken loginToken = new LoginToken(generatedNonce,Constants.MINDER_REALM);
        Utils.addToCurrentNonces(generatedNonce,loginToken);

        String contentType = "text/html";//application/json...
        String authInitHeader = Utils.prepareAuthenticateInitHeader(generatedNonce, Constants.MINDER_REALM);
        response().setContentType(contentType);
        response().setHeader(WWW_AUTHENTICATE, authInitHeader);

        return unauthorized(Constants.RESULT_FIRST_UNAUTHORIZED);
    }

    /**
     * 3.Client requests to authenticate the Minder with the authorization header
     * 4.Minder returns to Client with the response code 200.
     *
     * @return 200 Success
     */
    public static Result doLogin() {
        /// TODO:check the ACCEPT-TYPE header,
        // if the clients wants application/xml then send xml
        // if  ... ... . .... . application/json then send json
        String authorizationData = request().getHeader(AUTHORIZATION);

        //System.out.println(authorizationData);
        /*
          Parse client request
        */
        HashMap<String,String> clientRequest = Utils.createHashMapOfClientRequest(authorizationData);

        System.out.println("Validation Processes Started:");
        /*
        * Check user mail to validate a registered user
        *
        * */
        User user = User.findByEmail(clientRequest.get("username"));
        if(null == user){
            return unauthorized(Constants.RESULT_UNAUTHORIZED);
        }
        System.out.println("User checked!");

        /*
        * Check whether this is registered nonce in server side.
        * */
        if(!Utils.doesKeyExist(clientRequest.get("nonce"))){
            return unauthorized(Constants.RESULT_UNAUTHORIZED);
        }else if(Utils.isNonceExpired(Utils.getIssueTime(clientRequest.get("nonce")))){
            Utils.removeFromCurrentNonces(clientRequest.get("nonce"));
            response().setHeader("stale", "true");
            return unauthorized(Constants.RESULT_UNAUTHORIZED);
        }else if(!Utils.getRealmValue(clientRequest.get("nonce")).equals(clientRequest.get("realm"))){
            return unauthorized(Constants.RESULT_UNAUTHORIZED);
        }
        System.out.println("Nonce and realm checked");

        /*
        * Delete all expired nonce in DB.
        * */
        UserAuthentication.deleteAllExpiredNonces();
        System.out.println("All expired nonces are deleted");

        /*
        Check the request counter: Since it is the fist request, just checking whether it is 1 or not is enough.
         */
        String nc = clientRequest.get("nc");
        int checkedNC = Integer.parseInt(nc);
        if (1 != checkedNC){
            Utils.removeFromCurrentNonces(Utils.getRealmValue(clientRequest.get("nonce")));
            return unauthorized(Constants.RESULT_UNAUTHORIZED);
        }
        System.out.println("Request Counter checked!");

        /*
          Check the response value
         */
        if(!Utils.validateResponseValue(clientRequest.get("response"),
                clientRequest.get("username"), user.password,clientRequest.get("realm"),
                MethodType.POST, clientRequest.get("uri"), clientRequest.get("nonce"),
                clientRequest.get("cnonce"), clientRequest.get("nc"))){

            return unauthorized(Constants.RESULT_UNAUTHORIZED);
        }
        System.out.println("Response checked!");

        /*
        * Create new User Auth
        * */
        UserAuthentication.create(clientRequest.get("username"),clientRequest.get("realm"),
                clientRequest.get("nonce"),clientRequest.get("cnonce"),clientRequest.get("uri"),
                checkedNC);

        System.out.println("Validation Processes Finished!");

        System.out.print(authorizationData);
        return ok(Constants.RESULT_SUCCESS);

    }

}
