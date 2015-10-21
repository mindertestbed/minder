package rest.controllers.common;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 15/10/15.
 */
public class Constants {
    public static final String MINDER_REALM = "test@minder.gov.tr";
    public static final String AUTHORIZATION_DIGEST = "Digest";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int NONCE_VALIDITY_MILISECONDS = 300000; //300000miliseconds eq. to 300 seconds or 5 min.
    public static final String NONCE_VALIDITY_IN_DB = "5 SECOND"; //300000miliseconds eq. to 300 seconds or 5 min.


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

}
