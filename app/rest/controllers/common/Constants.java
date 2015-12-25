package rest.controllers.common;

/**
 * Collected constants of Minder's rest web services.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 15/10/15.
 */
public final class Constants {
    public static final String CLIENT_EXCEPTION = "CLIENT_EXCEPTION";
    public static final String SERVER_EXCEPTION = "SERVER_EXCEPTION";


    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String UNDEFINED = "UNDEFINED";
    public static final String AUTHENTICATION = "AUTHENTICATION";

    public static final String MINDER_REALM = "rest@minder.gov.tr";
    public static final String AUTHORIZATION_DIGEST = "xDigest";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int NONCE_VALIDITY_MILISECONDS = 300000; //300000miliseconds eq. to 300 seconds or 5 min.
    public static final String NONCE_VALIDITY_IN_DB = "1 HOUR"; //300000miliseconds eq. to 300 seconds or 5 min.


    public static final String RESULT_FIRST_UNAUTHORIZED = "401 Unauthorized";
    public static final String RESULT_UNAUTHORIZED = "401 Unauthorized: Make authentication request first";
    public static final String RESULT_SUCCESS = "200 Success";


    /*
    * Minder Content Validation Service Constants
    * */
    public static final String TYPE_XSD = "XSD";
    public static final String TYPE_SCHEMATRON = "SCHEMATRON";
    public static final String SUB_TYPE_URL = "URL";
    public static final String SUB_TYPE_PLAIN = "PLAIN";
    public static final String SUB_TYPE_ZIP = "ZIP";
    public static final String SUB_TYPE_JAR = "JAR";
    
    public static final String REPLY_TO_URL_ADDRESS =  "Gitb-ReplyToUrlAddress";

    private Constants(){
        //This prevents even the native class from calling this actor.
        throw new AssertionError();
    }
}
