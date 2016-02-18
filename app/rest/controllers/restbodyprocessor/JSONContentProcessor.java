package rest.controllers.restbodyprocessor;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.mvc.Http;

import java.io.ByteArrayInputStream;
import java.text.ParseException;


/**
 * The JSON content processor parse a request body or prepare a response body according using the Jackson library.
 * Jackson is a 3rd party library which is embedded playframework.
 * The class has generic structure which is unaware of the marshalling/unmarshalling object.
 *
 * @author: Melis Ozgur Cetinkaya Demir
 * @date: 16/10/15.
 */
public class JSONContentProcessor implements IRestContentProcessor {
    private String body;

    public JSONContentProcessor() {}

    public JSONContentProcessor(String body) {
        this.body = body;
    }

    public JSONContentProcessor(Http.RequestBody body) {
        String[] parts = body.toString().split("Some\\(");
        if(parts.length >0){
            String[] parts2 = parts[1].split("\\)\\,");
            if(parts2.length >0){
                this.body = parts2[0];
            }
        }
    }

    @Override
    public String prepareResponse(String className, Object o) throws ParseException {
       return Json.stringify(Json.toJson(o));
    }

    @Override
    public Object parseRequest(String className) throws ParseException{
        try {
            Class clazz = Class.forName(className);
            JsonNode json = Json.parse(new ByteArrayInputStream(body.getBytes()));

            return Json.fromJson(json,clazz);
        } catch (ClassNotFoundException e) {
            throw new ParseException("Error occured during the parsing of request's XML body. The details of the exception "+e.toString(),0);
        }
    }

	@Override
	public String getContentType() {
		return "application/json";
	}

}
