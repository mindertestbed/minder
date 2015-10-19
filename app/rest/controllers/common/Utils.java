package rest.controllers.common;

import global.Util;
import rest.controllers.LoginToken;
import rest.controllers.common.enumeration.MethodType;

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
 * Created by melis on 15/10/15.
 */
public class Utils {
    private static HashMap<String,LoginToken> currentServerNonces = new HashMap<String,LoginToken>();

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

}