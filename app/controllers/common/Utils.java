package controllers.common;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.api.http.Writeable$;
import play.api.libs.json.JsValue;
import play.api.mvc.Codec;
import play.api.mvc.Results$;
import utils.Util;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import security.AllowedRoles;
import security.Role;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.function.Function;

/**
 * Created by melis on 11/11/15.
 */
public class Utils extends Controller {

  @AllowedRoles({Role.TEST_DESIGNER})
  public static Result doEditField(Class<?> editorClass, Class<?> cls, JsonNode jsonNode, User localUser) {

    FieldUpdateDto dto = new FieldUpdateDto();
    dto.id = jsonNode.findPath("id").asInt();
    dto.field = jsonNode.findPath("field").asText();
    dto.newValue = jsonNode.findPath("newValue").asText();
    dto.converter = jsonNode.findPath("converter").asText();
    return doEditField(editorClass, cls, dto, localUser);
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public static Result doEditField(Class<?> editorClass, Class<?> cls, FieldUpdateDto dto, User localUser) {
    Function<String, Object> badRequestFunction = s -> badRequest(s);
    Function<String, Object> okFunction = s -> ok(s);
    return (Result) doEditField(editorClass, cls, dto, localUser, badRequestFunction, okFunction);
  }

  private static Object doEditField(Class<?> editorClass, Class<?> cls, FieldUpdateDto dto, User localUser, Function<String, Object> badRequestFunction, Function<String, Object> okFunction) {
    long id = dto.id;
    String field = dto.field;
    String newValue = dto.newValue;
    String converter = dto.converter;
    try {
      try {
        Util.checkField(editorClass, field, newValue);
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
        return badRequestFunction.apply(ex.getMessage());
      }

      //check access.
      // first lets see if we have a field named owner
      try {
        Field fld = cls.getDeclaredField("owner");

        //we have an owner. Lets verify if we are the rightfull owner

        String queryString = "find " + cls.getSimpleName() + " where id = :id";
        Query<?> query = Ebean.createQuery(cls, queryString);
        query.setParameter("id", id);
        Object o = query.findUnique();
        User user = (User) fld.get(o);


        if (localUser == null || !localUser.email.equals(user.email)) {
          return badRequestFunction.apply("You don't have the permission to modify this resource");
        }
      } catch (NoSuchFieldException ex) {

      }
      String updStatement = "update " + cls.getSimpleName() + " set " + field + " = :value where id = :id";
      Update<?> update = Ebean.createUpdate(cls, updStatement);

      Object newValueConverted = newValue;
      if (converter != null && converter.length() != 0)
        newValueConverted = Util.convertValue(converter, newValue);

      update.set("value", newValueConverted);
      update.set("id", id);
      update.execute();
      ObjectNode node = Json.newObject();
      node.put("value", newValueConverted.toString());
      return okFunction.apply(node.toString());
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      e.printStackTrace();
      return badRequestFunction.apply(sw.toString());
    }
  }

  @AllowedRoles({Role.TEST_DESIGNER})
  public static play.api.mvc.Result doEditFieldScala(Class<?> editorClass, Class<?> cls, FieldUpdateDto dto, User localUser) {
    Function<String, Object> badRequestFunction = s -> scalaBadRequest(s);
    Function<String, Object> okFunction = s -> scalaOk(s);
    return (play.api.mvc.Result) doEditField(editorClass, cls, dto, localUser, badRequestFunction, okFunction);
  }

  private static play.api.mvc.Result scalaBadRequest(String message) {
    return Results$.MODULE$.BadRequest().apply(message, Writeable$.MODULE$.wString(Codec.utf_8()));
  }

  private static play.api.mvc.Result scalaOk(String message) {
    return Results$.MODULE$.Ok().apply(message, Writeable$.MODULE$.wString(Codec.utf_8()));
  }

  // Gets the current timestamp in ISO 8601 Format : 2016-03-03T08:21:45+00:00
  public static String getCurrentTimeStamp() {
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
    }
    return javax.xml.bind.DatatypeConverter.printDateTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
  }
}
