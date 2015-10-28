package rest.controllers.common;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 15/10/15.
 */
public class Constants {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String UNDEFINED = "UNDEFINED";

    public static final String MINDER_REALM = "test@minder.gov.tr";
    public static final String AUTHORIZATION_DIGEST = "Digest";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int NONCE_VALIDITY_MILISECONDS = 300000; //300000miliseconds eq. to 300 seconds or 5 min.
    public static final String NONCE_VALIDITY_IN_DB = "1 HOUR"; //300000miliseconds eq. to 300 seconds or 5 min.


    public static final String RESULT_FIRST_UNAUTHORIZED = "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\" />\n" +
            "    <title>Error</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>401 Unauthorized</h1>\n" +
            "  </body>\n" +
            "</html>";


    public static final String RESULT_UNAUTHORIZED = "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\" />\n" +
            "    <title>Error</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>401 Unauthorized: Make authentication request first.</h1>\n" +
            "  </body>\n" +
            "</html>";

    public static final String RESULT_SUCCESS = "<html>\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\" />\n" +
            "    <title>Success</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>200 Success</h1>\n" +
            "  </body>\n" +
            "</html>";


    /*
    * Minder Content Validation Service Constants
    * */
    public static final String TYPE_XSD = "XSD";
    public static final String TYPE_SCHEMATRON = "SCHEMATRON";
    public static final String SUB_TYPE_URL = "URL";
    public static final String SUB_TYPE_PLAIN = "PLAIN";
    public static final String SUB_TYPE_ZIP = "ZIP";
    public static final String SUB_TYPE_JAR = "JAR";


}
