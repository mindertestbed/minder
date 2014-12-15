package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

public class Tester extends Controller {
  public static Result test(){
    String[] tdl = request().queryString().get("tdl");

    String tdlStr = null;
    try {
      tdlStr = new String(new sun.misc.BASE64Decoder().decodeBuffer(tdl[0]));
      System.out.println(tdlStr);
      return TesterScala.test(tdlStr);
    } catch (IOException e) {
      e.printStackTrace();
      return internalServerError();
    }
  }
}