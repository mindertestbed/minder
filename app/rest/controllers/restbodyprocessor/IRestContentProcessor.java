package rest.controllers.restbodyprocessor;

/**
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 * Strategy pattern
 */
public interface IRestContentProcessor {
    public String body = null;

    /*
   * Prepare the response that can be in format XML or JSON.
   * */
    public String prepareResponse(String className, Object o);

    /*
    * Parse the request that can be in format XML or JSON.
    * Fills the String body.
    * */
    public Object parseRequest(String className);


}
