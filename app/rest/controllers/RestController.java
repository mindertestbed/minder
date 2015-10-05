package rest.controllers;


import play.api.http.MediaRange;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

import play.api.libs.json.*;


/**
 * @author: yerlibilgin
 * @date: 26/09/15.
 */
public class RestController extends Controller {
  public static Result getAction(String link) {

    /*String contentType = resolveContentType();

    RestHandler handler = getHandler(contentType);*/

    Object service = resolveService(link);

   // service.

    final List<MediaRange> mediaRanges = request().acceptedTypes();
    for (MediaRange mr : mediaRanges) {
      System.out.println(">>>>" + mr.mediaType() + "/" + mr.mediaSubType() + " " + mr.productPrefix() + " : [" + mr.toString() + "]");
    }
    return ok(link + "\n" + request().toString()).as("text/plain");
  }


  public static Result postAction(String link) {
    final List<MediaRange> mediaRanges = request().acceptedTypes();
    for (MediaRange mr : mediaRanges) {
      System.out.println(">>>>" + mr.mediaType() + "/" + mr.mediaSubType() + " " + mr.productPrefix() + " : [" + mr.toString() + "]");
    }
    return ok(link + "\n" + request().toString()).as("text/plain");
  }
}
