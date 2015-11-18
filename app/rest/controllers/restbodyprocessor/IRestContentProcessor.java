package rest.controllers.restbodyprocessor;

import java.text.ParseException;

/**
 * This interfaces states the services provided by a content processor: prepare a REST response body and parse a received
 * REST request body. It is actually the implementation of the strategy pattern.
 *
 * For now, there is wto kind of content processor: XML and JSON. If it is requested more, all you need to do implement this
 * interface.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 *
 */
public interface IRestContentProcessor {
    /**
    * Prepare the response that can be in format XML or JSON.
    * */
    public String prepareResponse(String className, Object o) throws ParseException;

    /**
    * Parse the request that can be in format XML or JSON.
    * Fills the String body.
    * */
    public Object parseRequest(String className) throws ParseException;
    
    /**
     * 
     * @return the content type that will be set to response
     */
    public String getContentType();


}
