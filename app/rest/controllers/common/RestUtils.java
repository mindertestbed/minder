package rest.controllers.common;

import global.Util;
import models.User;
import models.UserAuthentication;
import play.mvc.Http;
import rest.controllers.login.LoginToken;
import rest.controllers.common.enumeration.MethodType;
import rest.controllers.restbodyprocessor.IRestContentProcessor;
import rest.controllers.restbodyprocessor.JSONContentProcessor;
import rest.controllers.restbodyprocessor.XMLContentProcessor;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class of Minder's rest web services.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 15/10/15.
 */
public class RestUtils {
    private static HashMap<String,LoginToken> currentServerNonces = new HashMap<String,LoginToken>();

    public static boolean verifyAuthentication(Http.Request request){
        System.out.println("verifyAuthentication");
        String authorizationData = String.valueOf(request.getHeader("Authorization"));
        System.out.println("authorizationData");

        /*
        *  Parse client request
        */
        HashMap<String,String> clientRequest = RestUtils.createHashMapOfClientRequest(authorizationData);
        System.out.println("Validation Processes Started:");

        UserAuthentication userAuthentication = UserAuthentication.findByServerNonceAndRealm(clientRequest.get("realm"), clientRequest.get("nonce"));
        if(null == userAuthentication){
            System.out.println("1");
            return false;
        }


        /*
        * Check the request counter: Since it is the fist request, just checking whether it is 1 or not is enough.
        */
        int nc  = Integer.parseInt(clientRequest.get("nc"));
        int ncInDB = userAuthentication.requestCounter;
        ncInDB = ncInDB + 1;

        if (ncInDB != nc) {
            System.out.println("Request counters are not compatible:" + ncInDB + " " + nc);
            return false;
        }
        System.out.println("Request Counter checked!");


        /*
        *  Check the response value
        */
        User user = User.findByEmail(clientRequest.get("username"));
        if(null == user){
            return false;
        }
        System.out.println("User checked!");

        if(!RestUtils.validateResponseValue(clientRequest.get("response"),
                clientRequest.get("username"), user.password, clientRequest.get("realm"),
                MethodType.POST, clientRequest.get("uri"), clientRequest.get("nonce"),
                clientRequest.get("cnonce"), clientRequest.get("nc"))){

            return false;
        }
        System.out.println("Response checked!");

        /*
        * Update nc for the User Auth
        */
        UserAuthentication.update(clientRequest.get("nonce"), ncInDB);

        return true;
    }

    public static String generateNonce(){
        Random ranGen = new SecureRandom();
        byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
        ranGen.setSeed(System.currentTimeMillis());
        ranGen.nextBytes(aesKey);

        return convertToHex(aesKey);
    }

    public static String prepareAuthenticateInitHeader(String serverNonce, String realm) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.AUTHORIZATION_DIGEST);
        sb.append("realm=\"");
        sb.append(realm);
        sb.append("\"");
        sb.append(",");
        sb.append("nonce=\"");
        sb.append(serverNonce);
        sb.append("\"");

        return sb.toString();

    }

    public static HashMap<String,String>  createHashMapOfClientRequest(String clientRequest) {
        HashMap<String,String> cRequest = new HashMap<String,String>();

        String patternString1 = (","); //"(\")|(,)";

        Pattern pattern = Pattern.compile(patternString1);
        Matcher matcher = pattern.matcher(clientRequest);

        String replaceAll = matcher.replaceAll("\n");
        System.out.println("replaceAll   = " + replaceAll);


        String REGEX = "(\\w*=(.+?)*)|(\\w*=\\d*)";

        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(replaceAll); // get a matcher object
        int count = 0;


        while(m.find()) {
            count++;
            System.out.println("==================");
            System.out.println("Match number "+count);
            String subStr = replaceAll.substring(m.start(),m.end());

            String patternString2 = ("\"");
            Pattern pattern2 = Pattern.compile(patternString2);
            Matcher matcher2 = pattern2.matcher(subStr);

            subStr = matcher2.replaceAll("");

            String delimeter = "=";
            String[] tokens = subStr.split(delimeter);
            System.out.println(tokens[0] + "-" + tokens[1]);
            cRequest.put(tokens[0],tokens[1]);
        }

        return cRequest;

    }

    // Hash map currentServerNonces' operations
    public static void addToCurrentNonces(String generatedNonce, LoginToken loginToken){
        currentServerNonces.put(generatedNonce,loginToken);
    }

    public static boolean doesKeyExist(String nonce){
        return currentServerNonces.containsKey(nonce);
    }

    public static String getRealmValue(String nonce){
        return currentServerNonces.get(nonce).getRealm();
    }

    public static Date getIssueTime(String nonce){
        return currentServerNonces.get(nonce).getIssueTime();
    }

    public static void removeFromCurrentNonces(String nonce){
        currentServerNonces.remove(nonce);
    }

    public static boolean isNonceExpired(Date issueTime){
        Date currTime = getCurrentDate();

        long issueTimeInMilis=issueTime.getTime();
        long totalTimeToValidity = issueTimeInMilis + Constants.NONCE_VALIDITY_MILISECONDS;
        System.out.println("issueTimeInMilis:"+issueTimeInMilis);
        System.out.println("totalTimeToValidity:"+totalTimeToValidity);

        long curreTimeInMilis = currTime.getTime();
        System.out.println("curreTimeInMilis:"+curreTimeInMilis);

        if(totalTimeToValidity > curreTimeInMilis ){
            System.out.println("false");
            return false;
        }
        return true;

    }

    //Hash
    public static boolean validateResponseValue(String response,String userName, byte[] hashedPassword, String realm, MethodType restVerb, String uri,String nonce,String cnonce,String nc){
        String calculatedResponse = calculateResponseValue(userName, convertToHex(hashedPassword),realm, restVerb.toString(), uri, nonce, cnonce, nc);

        return calculatedResponse.equals(response);

    }

    public static String calculateResponseValue(String userName, String password,String realm, String restVerb, String uri,String nonce,String cnonce,String nc){
        /*
        * Calculate HA1 = MD5("<userName>:<realm>:<password>")
        * */
        StringBuilder sb = new StringBuilder();
        sb.append(userName);
        sb.append(":");
        sb.append(realm);
        sb.append(":");
        sb.append(password);

        byte[] hashHA1 = Util.md5(sb.toString().getBytes());
        String HA1 = convertToHex(hashHA1);
        System.out.println("HA1=" + HA1);


        /*
        * Calculate HA2 = MD5("<verb>:<URi>")
        * */
        StringBuilder sb2 = new StringBuilder();
        sb2.append(restVerb);
        sb2.append(":");
        sb2.append(uri);
        byte[] hashHA2 = Util.md5(sb2.toString().getBytes());
        String HA2 = convertToHex(hashHA2);
        System.out.println("HA2="+HA2);

        /*
        * Calculate Response = MD5("HA1:\NONCE:\NC:CNONCE:\HA2)
        * */
        StringBuilder sb3 = new StringBuilder();
        sb3.append(HA1);
        sb3.append(":\\");
        sb3.append(nonce);
        sb3.append(":\\");
        sb3.append(nc);
        sb3.append(":");
        sb3.append(cnonce);
        sb3.append(":\\");
        sb3.append(HA2);
        byte[] hashHA3 = Util.md5(sb3.toString().getBytes());
        System.out.println("HA3="+convertToHex(hashHA3));
        return convertToHex(hashHA3);

    }

    private static String convertToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static Date getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        String currentTime = dateFormat.format(new Date());
        try {
            return dateFormat.parse(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Date getDate(long dateInMilis){
        DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        String time = dateFormat.format(new Date(dateInMilis));
        try {
            return dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static IRestContentProcessor createContentProcessor(String contentType, play.mvc.Http.RequestBody body) throws IllegalArgumentException{
        if(contentType.contains("text/xml") || contentType.contains("application/xml"))
            return new XMLContentProcessor();
        else if(contentType.contains("text/json") || contentType.contains("application/json"))
            return new JSONContentProcessor();

        throw new IllegalArgumentException("Content type is not defined. The defined types are text/xml and text/json. The received type is "+contentType);
    }

    public static IRestContentProcessor createContentProcessor(String contentType) throws IllegalArgumentException{
        if(contentType.contains("text/xml") || contentType.contains("application/xml"))
            return new XMLContentProcessor();
        else if(contentType.contains("text/json") || contentType.contains("application/json"))
            return new JSONContentProcessor();

        throw new IllegalArgumentException("Content type is not defined. The defined types are text/xml and text/json. The received type is "+contentType);

    }
}
